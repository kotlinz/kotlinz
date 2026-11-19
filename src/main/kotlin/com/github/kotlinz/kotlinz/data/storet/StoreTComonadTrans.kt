package com.github.kotlinz.kotlinz.data.storet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.ComonadTrans

/** ComonadTrans instance for [StoreT]: lowering evaluates every lookup function at the current position. */
interface StoreTComonadTrans<S, W> : ComonadTrans<W, K2<StoreT.T, S, W>>, StoreTComonad<S, W> {
    override fun <A> lower(v: K1<K2<StoreT.T, S, W>, A>): K1<W, A> = StoreT.narrow(v).lower()
}
