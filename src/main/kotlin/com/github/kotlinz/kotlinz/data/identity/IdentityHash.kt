package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.hash.Hash

/** [Hash] instance for [Identity], consistent with [IdentityEq]. */
interface IdentityHash<A>: Hash<Identity<A>>, IdentityEq<A> {
  val hashA: Hash<A>
  override val eqA: Eq<A> get() = hashA

  override fun hash(a: Identity<A>): Int = hashA.hash(a.value)
}

/** Returns the [Hash] instance for [Identity] derived from the given element instances. */
fun <A> Identity.Companion.hash(hashA: Hash<A>): Hash<Identity<A>> = object: IdentityHash<A> { override val hashA = hashA }
