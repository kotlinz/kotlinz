package com.github.kotlinz.kotlinz.data.nonemptymap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.applicative.Apply

/** Traversable instance for [NonEmptyMap], running the effects of the values in insertion order. */
interface NonEmptyMapTraversable<K>: Traversable<K1<NonEmptyMap.T, K>>, NonEmptyMapFunctor<K>, NonEmptyMapFoldable<K> {
  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<K1<NonEmptyMap.T, K>, A>): K1<G, K1<K1<NonEmptyMap.T, K>, B>> =
    traverseValues(applicative, f, NonEmptyMap.narrow(v))
}

/** Traverses the values of [map] left to right; only [Apply] is needed because the map is never empty. */
internal fun <G, K, A, B> traverseValues(apply: Apply<G>, f: (A) -> K1<G, B>, map: NonEmptyMap<K, A>): K1<G, K1<K1<NonEmptyMap.T, K>, B>> {
  val (firstKey, firstValue) = map.head
  val first = apply.fmap({ b: B -> linkedMapOf(firstKey to b) }, f(firstValue))
  val all = map.toMap().entries.drop(1).fold(first) { acc, (k, a) ->
    apply.ap(apply.fmap({ m: LinkedHashMap<K, B> -> { b: B -> LinkedHashMap(m).apply { put(k, b) } } }, acc), f(a))
  }
  return apply.fmap({ m: LinkedHashMap<K, B> -> NonEmptyMap.wrap(m) }, all)
}
