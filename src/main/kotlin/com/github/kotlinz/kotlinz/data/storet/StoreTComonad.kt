package com.github.kotlinz.kotlinz.data.storet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [StoreT]; duplicates by extending the base comonad of lookup functions. */
interface StoreTComonad<S, W> : Comonad<K2<StoreT.T, S, W>>, StoreTFunctor<S, W> {
    override fun <A> extract(v: K1<K2<StoreT.T, S, W>, A>): A = StoreT.narrow(v).let { it.peek(it.pos) }

    override fun <A> duplicate(v: K1<K2<StoreT.T, S, W>, A>): StoreT<S, W, K1<K2<StoreT.T, S, W>, A>> {
        val storeT = StoreT.narrow(v)
        val run = comonad.extend({ w: K1<W, (S) -> A> -> { s: S -> StoreT(comonad, w, s) as K1<K2<StoreT.T, S, W>, A> } }, storeT.run)
        return StoreT(comonad, run, storeT.pos)
    }

    override fun <A, B> extend(f: (K1<K2<StoreT.T, S, W>, A>) -> B, v: K1<K2<StoreT.T, S, W>, A>): StoreT<S, W, B> =
        StoreT.narrow(fmap(f, duplicate(v)))
}
