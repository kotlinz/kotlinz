package com.github.kotlinz.kotlinz.data.tracedt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.ComonadTraced

/** ComonadTraced instance for [TracedT]. */
interface TracedTComonadTraced<M : Monoid<*, M>, W> : ComonadTraced<K2<TracedT.T, M, W>, M>, TracedTComonad<M, W> {
    override fun <A> trace(m: M, v: K1<K2<TracedT.T, M, W>, A>): A = TracedT.narrow(v).trace(m)
}
