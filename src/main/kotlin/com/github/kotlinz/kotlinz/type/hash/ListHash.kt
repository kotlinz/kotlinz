package com.github.kotlinz.kotlinz.type.hash

import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.eq.ListEq

/** [Hash] instance for [List]: combines the element hashes of [hashA] in order. */
interface ListHash<A>: Hash<List<A>>, ListEq<A> {
  val hashA: Hash<A>
  override val eqA: Eq<A> get() = hashA

  override fun hash(a: List<A>): Int = Hash.combine(*a.map(hashA::hash).toIntArray())
}
