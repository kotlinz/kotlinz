package com.github.kotlinz.kotlinz.data.nonemptyset

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable1

/** Foldable1 instance for [NonEmptySet], reducing the elements in insertion order. */
interface NonEmptySetFoldable1: Foldable1<NonEmptySet.T>, NonEmptySetFoldable {
  override fun <A> head(v: K1<NonEmptySet.T, A>): A = NonEmptySet.narrow(v).head
}
