package com.github.kotlinz.kotlinz.type.ring

/**
 * A commutative [Ring] in which every non-zero element has a multiplicative inverse, which gives division.
 *
 * Laws (in addition to the [Ring] laws):
 * - Commutative multiplication: `times(a, b) == times(b, a)`
 * - Multiplicative inverse: `a != zero()` implies `times(a, reciprocal(a)) == one()`
 * - Division: `div(a, b) == times(a, reciprocal(b))`
 *
 * The result of `reciprocal(zero())` is unspecified by the laws; each instance documents its behavior.
 *
 * @see Ring
 */
interface Field<A, Self: Field<A, Self>>: Ring<A, Self> {
  /** Returns the multiplicative inverse of `a`. */
  fun reciprocal(a: Self): Self
  /** Divides `a` by `b`. */
  fun div(a: Self, b: Self): Self = times(a, reciprocal(b))

  companion object {
    /** Returns the multiplicative inverse of `a` using the instance created from `type`. */
    fun <A: Field<*, A>> reciprocal(type: Class<A>, a: A): A = instance(type).reciprocal(a)
    /** Divides `a` by `b` using the instance created from `type`. */
    fun <A: Field<*, A>> div(type: Class<A>, a: A, b: A): A = instance(type).div(a, b)

    private fun <A: Field<*, A>> instance(type: Class<A>): A = type.getConstructor().newInstance()
  }
}
