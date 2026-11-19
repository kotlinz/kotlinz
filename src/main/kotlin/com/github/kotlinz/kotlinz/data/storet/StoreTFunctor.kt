package com.github.kotlinz.kotlinz.data.storet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Functor instance for [StoreT]; requires the base [Comonad] via [comonad]. */
interface StoreTFunctor<S, W> : Functor<K2<StoreT.T, S, W>> {
    /** The base comonad. */
    val comonad: Comonad<W>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<StoreT.T, S, W>, A>): StoreT<S, W, B> {
        val storeT = StoreT.narrow(v)
        return StoreT(comonad, comonad.fmap({ g: (S) -> A -> { s: S -> f(g(s)) } }, storeT.run), storeT.pos)
    }
}
