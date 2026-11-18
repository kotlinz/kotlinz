package com.github.kotlinz.kotlinz.data.monoidof

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A first-class monoid dictionary for values of type A.
 *
 * Use it when a monoid has to be passed around as a value, e.g. for plain types such as `Int`
 * that cannot implement [Monoid] themselves, or to derive a new monoid via an isomorphism.
 * Has an instance for Invariant.
 *
 * ```kotlin
 * val sum = MonoidOf(0) { a: Int, b: Int -> a + b }
 * val strings = MonoidOf.from(StringMonoid::class.java).imap({ it.value }, { it.toStringMonoid() })
 * strings.combine("foo", "bar") // "foobar"
 * ```
 */
class MonoidOf<A>(val empty: A, val combine: (A, A) -> A): K1<MonoidOf.T, A> {
  /** Higher-kinded witness tag for [MonoidOf]. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [MonoidOf]. */
    fun <A> narrow(v: K1<T, A>): MonoidOf<A> = v as MonoidOf<A>
    /** Builds a dictionary from a [Monoid] implementation class. */
    fun <A: Monoid<*, A>> from(type: Class<A>): MonoidOf<A> = MonoidOf(Monoid.mzero(type)) { a, b -> Monoid.op(type, a, b) }
  }

  // Invariant
  /** Transports this monoid along the isomorphism [f] / [g]. */
  fun <B> imap(f: (A) -> B, g: (B) -> A): MonoidOf<B> = invariant.imap(f, g, this)
  private val invariant = object: MonoidOfInvariant {}
}
