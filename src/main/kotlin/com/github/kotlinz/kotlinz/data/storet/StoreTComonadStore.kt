package com.github.kotlinz.kotlinz.data.storet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.ComonadStore

/** ComonadStore instance for [StoreT]. */
interface StoreTComonadStore<S, W> : ComonadStore<K2<StoreT.T, S, W>, S>, StoreTComonad<S, W> {
    override fun <A> pos(v: K1<K2<StoreT.T, S, W>, A>): S = StoreT.narrow(v).pos
    override fun <A> peek(s: S, v: K1<K2<StoreT.T, S, W>, A>): A = StoreT.narrow(v).peek(s)
    override fun <A> seek(s: S, v: K1<K2<StoreT.T, S, W>, A>): StoreT<S, W, A> = StoreT.narrow(v).seek(s)
}
