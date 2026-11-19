package com.github.kotlinz.kotlinz.data.cofreet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.ComonadTrans

/** ComonadTrans instance for [CofreeT]: lowering keeps only the heads of the top layer. */
interface CofreeTComonadTrans<F, W> : ComonadTrans<W, K2<CofreeT.T, F, W>>, CofreeTComonad<F, W> {
    override fun <A> lower(v: K1<K2<CofreeT.T, F, W>, A>): K1<W, A> = CofreeT.narrow(v).lower()
}
