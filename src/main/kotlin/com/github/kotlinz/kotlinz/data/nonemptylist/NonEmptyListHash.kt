package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.hash.Hash

/** [Hash] instance for [NonEmptyList], consistent with [NonEmptyListEq]. */
interface NonEmptyListHash<A>: Hash<NonEmptyList<A>>, NonEmptyListEq<A> {
  val hashA: Hash<A>
  override val eqA: Eq<A> get() = hashA

  override fun hash(a: NonEmptyList<A>): Int = Hash.list(hashA).hash(a.toList())
}

/** Returns the [Hash] instance for [NonEmptyList] derived from the given element instances. */
fun <A> NonEmptyList.Companion.hash(hashA: Hash<A>): Hash<NonEmptyList<A>> = object: NonEmptyListHash<A> { override val hashA = hashA }
