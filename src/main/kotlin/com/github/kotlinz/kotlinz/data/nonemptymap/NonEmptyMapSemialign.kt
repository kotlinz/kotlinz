package com.github.kotlinz.kotlinz.data.nonemptymap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.type.align.Semialign

/** Semialign instance for [NonEmptyMap]: the result has the union of both key sets, first map's keys first. */
interface NonEmptyMapSemialign<K>: Semialign<K1<NonEmptyMap.T, K>>, NonEmptyMapFunctor<K> {
  override fun <A, B, C> alignWith(f: (Ior<A, B>) -> C, a: K1<K1<NonEmptyMap.T, K>, A>, b: K1<K1<NonEmptyMap.T, K>, B>): NonEmptyMap<K, C> {
    val left = NonEmptyMap.narrow(a).toMap()
    val right = NonEmptyMap.narrow(b).toMap()
    return NonEmptyMap.wrap((left.keys + right.keys).associateWithTo(LinkedHashMap()) { k -> f(iorAt(left, right, k)) })
  }

  override fun <A, B> align(a: K1<K1<NonEmptyMap.T, K>, A>, b: K1<K1<NonEmptyMap.T, K>, B>): NonEmptyMap<K, Ior<A, B>> =
    NonEmptyMap.narrow(super.align(a, b))

  private fun <A, B> iorAt(left: Map<K, A>, right: Map<K, B>, key: K): Ior<A, B> = when {
    key !in right -> Ior.left(left.getValue(key))
    key !in left -> Ior.right(right.getValue(key))
    else -> Ior.both(left.getValue(key), right.getValue(key))
  }
}
