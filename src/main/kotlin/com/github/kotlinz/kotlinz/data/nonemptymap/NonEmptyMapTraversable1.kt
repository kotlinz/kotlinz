package com.github.kotlinz.kotlinz.data.nonemptymap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable1
import com.github.kotlinz.kotlinz.type.applicative.Apply

/** Traversable1 instance for [NonEmptyMap]: traverses the values with an [Apply] in insertion order. */
interface NonEmptyMapTraversable1<K>: Traversable1<K1<NonEmptyMap.T, K>>, NonEmptyMapTraversable<K>, NonEmptyMapFoldable1<K> {
  override fun <G, A, B> traverse1(apply: Apply<G>, f: (A) -> K1<G, B>, v: K1<K1<NonEmptyMap.T, K>, A>): K1<G, K1<K1<NonEmptyMap.T, K>, B>> =
    traverseValues(apply, f, NonEmptyMap.narrow(v))
}
