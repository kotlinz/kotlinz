package com.github.kotlinz.kotlinz.data.nonemptymap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

/** Foldable instance for [NonEmptyMap], visiting the values in insertion order. */
interface NonEmptyMapFoldable<K>: Foldable<K1<NonEmptyMap.T, K>> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<K1<NonEmptyMap.T, K>, A>): B = toList(v).fold(initial, f)
  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<K1<NonEmptyMap.T, K>, A>): B = toList(v).foldRight(initial, f)
  override fun <A> toList(v: K1<K1<NonEmptyMap.T, K>, A>): List<A> = NonEmptyMap.narrow(v).toMap().values.toList()
  override fun <A> length(v: K1<K1<NonEmptyMap.T, K>, A>): Int = NonEmptyMap.narrow(v).size
}
