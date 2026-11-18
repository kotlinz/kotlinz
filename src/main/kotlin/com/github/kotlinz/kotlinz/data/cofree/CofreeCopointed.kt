package com.github.kotlinz.kotlinz.data.cofree

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.pointed.Copointed

/** Copointed instance for [Cofree]; extracts the head. */
interface CofreeCopointed<F>: Copointed<K1<Cofree.T, F>> {
  override fun <A> extract(v: K1<K1<Cofree.T, F>, A>): A = Cofree.narrow(v).head
}
