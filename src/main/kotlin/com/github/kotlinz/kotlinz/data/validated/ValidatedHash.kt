package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.type.eq.Eq
import com.github.kotlinz.kotlinz.type.hash.Hash

/** [Hash] instance for [Validated], consistent with [ValidatedEq]. */
interface ValidatedHash<E, A>: Hash<Validated<E, A>>, ValidatedEq<E, A> {
  val hashE: Hash<E>
  val hashA: Hash<A>
  override val eqE: Eq<E> get() = hashE
  override val eqA: Eq<A> get() = hashA

  override fun hash(a: Validated<E, A>): Int = when (a) {
    is Validated.Invalid -> Hash.combine(0, hashE.hash(a.error))
    is Validated.Valid -> Hash.combine(1, hashA.hash(a.value))
  }
}

/** Returns the [Hash] instance for [Validated] derived from the given element instances. */
fun <E, A> Validated.Companion.hash(hashE: Hash<E>, hashA: Hash<A>): Hash<Validated<E, A>> = object: ValidatedHash<E, A> { override val hashE = hashE; override val hashA = hashA }
