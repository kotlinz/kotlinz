package com.github.kotlinz.kotlinz.type.eq

/**
 * A type class for type-safe equivalence of values of type [A].
 *
 * Unlike `==`, an `Eq` instance is chosen explicitly, so values can be compared by a notion of equality
 * other than [Any.equals], and derived instances can be built from element instances
 * (e.g. `Maybe.eq(Int.eq)`).
 *
 * Laws:
 * - Reflexivity: `eqv(a, a)`
 * - Symmetry: `eqv(a, b) == eqv(b, a)`
 * - Transitivity: `eqv(a, b) && eqv(b, c)` implies `eqv(a, c)`
 *
 * ```kotlin
 * Int.eq.eqv(1, 1)                     // true
 * Maybe.eq(Int.eq).neqv(Maybe.Just(1), Maybe.None()) // true
 * ```
 */
interface Eq<A> {
  /** Returns true when [a] and [b] are equivalent. */
  fun eqv(a: A, b: A): Boolean
  /** Returns true when [a] and [b] are not equivalent. */
  fun neqv(a: A, b: A): Boolean = !eqv(a, b)

  companion object {
    /** An instance that delegates to [Any.equals]. */
    fun <A> fromEquals(): Eq<A> = EqualsEq()
    /** An instance built from the equivalence function [f]. */
    fun <A> by(f: (A, A) -> Boolean): Eq<A> = object: Eq<A> {
      override fun eqv(a: A, b: A): Boolean = f(a, b)
    }
    /** Element-wise equivalence of lists using [eqA]. */
    fun <A> list(eqA: Eq<A>): Eq<List<A>> = object: ListEq<A> { override val eqA = eqA }
  }
}
