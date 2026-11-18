package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [MaybeT]; requires a `Monad<F>` for the underlying effect. */
interface MaybeTMonad<F> : Monad<K1<MaybeT.T, F>>, MaybeTApplicative<F>, MaybeTFunctor<F> {
    override fun <A> join(v: K1<K1<MaybeT.T, F>, K1<K1<MaybeT.T, F>, A>>): MaybeT<F, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K1<MaybeT.T, F>, B>, v: K1<K1<MaybeT.T, F>, A>): MaybeT<F, B> {
        val run = monad.bind({ m: Maybe<A> ->
            when (m) {
                is Maybe.Just -> MaybeT.narrow(f(m.value)).run
                is Maybe.None -> monad.pure(Maybe.None<B>())
            }
        }, MaybeT.narrow(v).run)
        return MaybeT(monad, run)
    }
}
