package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.pointed.Copointed

/** Copointed instance for [Tuple2], extracting the second component. */
interface Tuple2Copointed<S>: Copointed<K1<Tuple2.T, S>> {
  override fun <A> extract(v: K1<K1<Tuple2.T, S>, A>): A = Tuple2.narrow(v).second
}
