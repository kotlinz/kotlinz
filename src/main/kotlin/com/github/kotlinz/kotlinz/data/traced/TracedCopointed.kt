package com.github.kotlinz.kotlinz.data.traced

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.pointed.Copointed

/** Copointed instance for [Traced]; extracts at `mzero` of [type]. */
interface TracedCopointed<M: Monoid<*, M>>: Copointed<K1<Traced.T, M>> {
  /** The Monoid class of `M`. */
  val type: Class<M>

  override fun <A> extract(v: K1<K1<Traced.T, M>, A>): A = Traced.narrow(v).run(Monoid.mzero(type))
}
