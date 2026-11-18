package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

/** Foldable instance for [NonEmptyList]. */
interface NonEmptyListFoldable: Foldable<NonEmptyList.T> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<NonEmptyList.T, A>): B = NonEmptyList.narrow(v).toList().fold(initial, f)
  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<NonEmptyList.T, A>): B = NonEmptyList.narrow(v).toList().foldRight(initial, f)
  override fun <A> toList(v: K1<NonEmptyList.T, A>): List<A> = NonEmptyList.narrow(v).toList()
  override fun <A> isEmpty(v: K1<NonEmptyList.T, A>): Boolean = false
}
