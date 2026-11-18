package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.pointed.Copointed

/** Copointed instance for [NonEmptyList]; extracts the head. */
interface NonEmptyListCopointed: Copointed<NonEmptyList.T> {
  override fun <A> extract(v: K1<NonEmptyList.T, A>): A = NonEmptyList.narrow(v).head
}
