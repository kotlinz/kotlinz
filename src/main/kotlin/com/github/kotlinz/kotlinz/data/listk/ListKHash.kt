package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.hash.Hash

/** [Hash] instance for [ListK], consistent with [ListKEq]. */
interface ListKHash<A>: Hash<ListK<A>>, ListKEq<A> {
  val hashA: Hash<A>
  override val eqA: Eq<A> get() = hashA

  override fun hash(a: ListK<A>): Int = Hash.list(hashA).hash(a.value)
}

/** Returns the [Hash] instance for [ListK] derived from the given element instances. */
fun <A> ListK.Companion.hash(hashA: Hash<A>): Hash<ListK<A>> = object: ListKHash<A> { override val hashA = hashA }
