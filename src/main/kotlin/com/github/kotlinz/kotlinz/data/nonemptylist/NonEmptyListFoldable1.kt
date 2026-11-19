package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable1

/** Foldable1 instance for [NonEmptyList]. */
interface NonEmptyListFoldable1: Foldable1<NonEmptyList.T>, NonEmptyListFoldable {
  override fun <A> head(v: K1<NonEmptyList.T, A>): A = NonEmptyList.narrow(v).head
}
