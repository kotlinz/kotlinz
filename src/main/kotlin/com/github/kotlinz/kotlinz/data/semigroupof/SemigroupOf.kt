package com.github.kotlinz.kotlinz.data.semigroupof

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Semigroup

/**
 * A first-class semigroup dictionary for values of type A.
 *
 * Use it to pass an associative `combine` operation around as a value, e.g. for types that
 * cannot implement [Semigroup] themselves. Has an Invariant instance ([SemigroupOfInvariant]).
 *
 * ```kotlin
 * val max = SemigroupOf { a: Int, b: Int -> maxOf(a, b) }
 * max.combine(1, 3) // 3
 * val strings = SemigroupOf.from(StringMonoid::class.java).imap({ it.value }, { it.toStringMonoid() })
 * strings.combine("foo", "bar") // "foobar"
 * ```
 *
 * @property combine the associative binary operation.
 */
class SemigroupOf<A>(val combine: (A, A) -> A): K1<SemigroupOf.T, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded [SemigroupOf] to its concrete type. */
    fun <A> narrow(v: K1<T, A>): SemigroupOf<A> = v as SemigroupOf<A>
    /** Builds a dictionary from a [Semigroup] implementation class. */
    fun <A: Semigroup<*, A>> from(type: Class<A>): SemigroupOf<A> = SemigroupOf { a, b -> Semigroup.op(type, a, b) }
  }

  // Invariant
  /** Transports this semigroup along the isomorphism `f` / `g`. */
  fun <B> imap(f: (A) -> B, g: (B) -> A): SemigroupOf<B> = invariant.imap(f, g, this)
  private val invariant = object: SemigroupOfInvariant {}
}
