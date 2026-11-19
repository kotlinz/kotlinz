package com.github.kotlinz.kotlinz.data.moore

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.pointed.Copointed

/** Copointed instance for [Moore]; extracts the current output. */
interface MooreCopointed<I>: Copointed<K2<Moore.T, Unit, I>> {
  override fun <A> extract(v: K1<K2<Moore.T, Unit, I>, A>): A = Moore.narrow(v).output
}
