package com.github.kotlinz.kotlinz.data.contt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Functor instance for [ContT]. */
interface ContTFunctor<R, F> : Functor<K2<ContT.T, R, F>> {
    /** The underlying monad. */
    val monad: Monad<F>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<ContT.T, R, F>, A>): ContT<R, F, B> {
        val cont = ContT.narrow(v)
        return ContT(monad) { k -> cont.run { a -> k(f(a)) } }
    }
}
