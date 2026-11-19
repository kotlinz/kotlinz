package com.github.kotlinz.kotlinz.data.contt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [ContT]; the function computation runs first. */
interface ContTApplicative<R, F> : Applicative<K2<ContT.T, R, F>>, ContTFunctor<R, F> {
    override fun <A> pure(v: A): ContT<R, F, A> = ContT(monad) { k -> k(v) }

    override fun <A, B> ap(f: K1<K2<ContT.T, R, F>, (A) -> B>, v: K1<K2<ContT.T, R, F>, A>): ContT<R, F, B> {
        val contf = ContT.narrow(f)
        val cont = ContT.narrow(v)
        return ContT(monad) { k -> contf.run { g -> cont.run { a -> k(g(a)) } } }
    }
}
