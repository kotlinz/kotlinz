package com.github.kotlinz.kotlinz.data.nonemptymap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable1

/** Foldable1 instance for [NonEmptyMap], reducing the values in insertion order. */
interface NonEmptyMapFoldable1<K>: Foldable1<K1<NonEmptyMap.T, K>>, NonEmptyMapFoldable<K> {
  override fun <A> head(v: K1<K1<NonEmptyMap.T, K>, A>): A = NonEmptyMap.narrow(v).head.second
}
