package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.hash.Hash

/** [Hash] instance for [Maybe], consistent with [MaybeEq]. */
interface MaybeHash<A>: Hash<Maybe<A>>, MaybeEq<A> {
  val hashA: Hash<A>
  override val eqA: Eq<A> get() = hashA

  override fun hash(a: Maybe<A>): Int = when (a) {
    is Maybe.Just -> Hash.combine(1, hashA.hash(a.value))
    is Maybe.None -> 0
  }
}

/** Returns the [Hash] instance for [Maybe] derived from the given element instances. */
fun <A> Maybe.Companion.hash(hashA: Hash<A>): Hash<Maybe<A>> = object: MaybeHash<A> { override val hashA = hashA }
