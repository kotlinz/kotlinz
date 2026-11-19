package com.github.kotlinz.kotlinz.type.hash

import com.github.kotlinz.kotlinz.type.eq.Eq

/**
 * A type class for hashing values of type [A] consistently with an [Eq] notion of equivalence.
 *
 * Laws (in addition to the [Eq] laws):
 * - Consistency: `eqv(a, b)` implies `hash(a) == hash(b)`
 *
 * ```kotlin
 * Int.hash.hash(1)                                        // 1
 * Maybe.hash(String.hash).eqv(Maybe.Just("a"), Maybe.Just("a")) // true
 * ```
 */
interface Hash<A>: Eq<A> {
  /** Returns a hash code for [a] that agrees with [eqv]. */
  fun hash(a: A): Int

  companion object {
    /** An instance that delegates to [Any.equals] and [Any.hashCode]. */
    fun <A> fromHashCode(): Hash<A> = HashCodeHash()
    /** An instance built from the equivalence [eq] and the hash function [f], which must agree with [eq]. */
    fun <A> by(eq: Eq<A>, f: (A) -> Int): Hash<A> = object: Hash<A> {
      override fun eqv(a: A, b: A): Boolean = eq.eqv(a, b)
      override fun hash(a: A): Int = f(a)
    }
    /** Element-wise hashing of lists using [hashA]. */
    fun <A> list(hashA: Hash<A>): Hash<List<A>> = object: ListHash<A> { override val hashA = hashA }
    /** Combines hash codes in order, the same way [List.hashCode] does. */
    fun combine(vararg hashes: Int): Int = hashes.fold(1) { acc, h -> 31 * acc + h }
  }
}
