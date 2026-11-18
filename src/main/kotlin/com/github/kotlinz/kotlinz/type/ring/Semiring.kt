package com.github.kotlinz.kotlinz.type.ring

/**
 * A type with two monoidal operations, addition and multiplication, where multiplication
 * distributes over addition.
 *
 * Uses the same `Self` / no-arg constructor encoding as [com.github.kotlinz.kotlinz.type.group.Semigroup].
 *
 * Laws:
 * - `(plus, zero)` is a commutative monoid and `(times, one)` is a monoid
 * - Distributivity: `times(a, plus(b, c)) == plus(times(a, b), times(a, c))` (and on the right)
 * - Annihilation: `times(zero(), a) == zero() == times(a, zero())`
 *
 * @see com.github.kotlinz.kotlinz.type.group.Monoid
 */
interface Semiring<A, Self: Semiring<A, Self>> {
  /** Addition. */
  fun plus(a: Self, b: Self): Self
  /** Multiplication. */
  fun times(a: Self, b: Self): Self
  /** The identity of [plus]. */
  fun zero(): Self
  /** The identity of [times]. */
  fun one(): Self

  companion object {
    /** Adds `a` and `b` using the instance created from `type`. */
    fun <A: Semiring<*, A>> plus(type: Class<A>, a: A, b: A): A = instance(type).plus(a, b)
    /** Multiplies `a` and `b` using the instance created from `type`. */
    fun <A: Semiring<*, A>> times(type: Class<A>, a: A, b: A): A = instance(type).times(a, b)
    /** Returns the additive identity of `type`. */
    fun <A: Semiring<*, A>> zero(type: Class<A>): A = instance(type).zero()
    /** Returns the multiplicative identity of `type`. */
    fun <A: Semiring<*, A>> one(type: Class<A>): A = instance(type).one()

    private fun <A: Semiring<*, A>> instance(type: Class<A>): A = type.getConstructor().newInstance()
  }
}
