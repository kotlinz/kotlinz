package com.github.kotlinz.kotlinz.data.nonemptyset

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

/** Foldable instance for [NonEmptySet], visiting the elements in insertion order. */
interface NonEmptySetFoldable: Foldable<NonEmptySet.T> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<NonEmptySet.T, A>): B = NonEmptySet.narrow(v).fold(initial, f)
  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<NonEmptySet.T, A>): B = toList(v).foldRight(initial, f)
  override fun <A> toList(v: K1<NonEmptySet.T, A>): List<A> = NonEmptySet.narrow(v).toSet().toList()
  override fun <A> length(v: K1<NonEmptySet.T, A>): Int = NonEmptySet.narrow(v).size
}
