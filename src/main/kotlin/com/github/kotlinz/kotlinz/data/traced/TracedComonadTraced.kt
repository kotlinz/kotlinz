package com.github.kotlinz.kotlinz.data.traced

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.ComonadTraced

/** ComonadTraced instance for [Traced]; requires the Monoid class via [type]. */
interface TracedComonadTraced<M: Monoid<*, M>>: ComonadTraced<K1<Traced.T, M>, M>, TracedComonad<M> {
  override fun <A> trace(m: M, v: K1<K1<Traced.T, M>, A>): A = Traced.narrow(v).run(m)
}
