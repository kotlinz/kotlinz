package com.github.kotlinz.kotlinz.data.tracedt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.ComonadTrans

/** ComonadTrans instance for [TracedT]: lowering runs every function at `mzero`. */
interface TracedTComonadTrans<M : Monoid<*, M>, W> : ComonadTrans<W, K2<TracedT.T, M, W>>, TracedTComonad<M, W> {
    override fun <A> lower(v: K1<K2<TracedT.T, M, W>, A>): K1<W, A> = TracedT.narrow(v).lower()
}
