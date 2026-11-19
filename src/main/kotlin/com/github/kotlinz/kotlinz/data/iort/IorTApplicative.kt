package com.github.kotlinz.kotlinz.data.iort

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Semigroup

/** Applicative instance for [IorT]; effects run function first and left values accumulate. */
interface IorTApplicative<F, A: Semigroup<*, A>> : Applicative<K2<IorT.T, F, A>>, IorTFunctor<F, A> {
    override fun <B> pure(v: B): IorT<F, A, B> = IorT(monad, type, monad.pure(Ior.Right(v)))

    override fun <B, C> ap(f: K1<K2<IorT.T, F, A>, (B) -> C>, v: K1<K2<IorT.T, F, A>, B>): IorT<F, A, C> {
        return chain(IorT.narrow(f)) { g -> fmap(g, v) }
    }

    /** Runs [v] and continues with [f] on its right value, prepending any left value it produced. */
    fun <B, C> chain(v: IorT<F, A, B>, f: (B) -> IorT<F, A, C>): IorT<F, A, C> {
        val run = monad.bind({ ior: Ior<A, B> ->
            when (ior) {
                is Ior.Left -> monad.pure(Ior.Left<A, C>(ior.value))
                is Ior.Right -> f(ior.value).run
                is Ior.Both -> monad.fmap({ next: Ior<A, C> -> prependLeft(ior.left, next) }, f(ior.right).run)
            }
        }, v.run)
        return IorT(monad, type, run)
    }

    /** Combines [a] in front of the left value of [v], turning a [Ior.Right] into [Ior.Both]. */
    fun <C> prependLeft(a: A, v: Ior<A, C>): Ior<A, C> = when (v) {
        is Ior.Left -> Ior.Left(Semigroup.op(type, a, v.value))
        is Ior.Right -> Ior.Both(a, v.value)
        is Ior.Both -> Ior.Both(Semigroup.op(type, a, v.left), v.right)
    }
}
