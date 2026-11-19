package com.github.kotlinz.kotlinz.type.ring

/**
 * A [Semiring] with additive inverses, which gives subtraction.
 *
 * Laws (in addition to the [Semiring] laws):
 * - Additive inverse: `plus(a, negate(a)) == zero()`
 * - Subtraction: `minus(a, b) == plus(a, negate(b))`
 *
 * @see Semiring
 * @see Field
 */
interface Ring<A, Self: Ring<A, Self>>: Semiring<A, Self> {
  /** Returns the additive inverse of `a`. */
  fun negate(a: Self): Self
  /** Subtracts `b` from `a`. */
  fun minus(a: Self, b: Self): Self = plus(a, negate(b))

  companion object {
    /** Returns the additive inverse of `a` using the instance created from `type`. */
    fun <A: Ring<*, A>> negate(type: Class<A>, a: A): A = instance(type).negate(a)
    /** Subtracts `b` from `a` using the instance created from `type`. */
    fun <A: Ring<*, A>> minus(type: Class<A>, a: A, b: A): A = instance(type).minus(a, b)

    private fun <A: Ring<*, A>> instance(type: Class<A>): A = type.getConstructor().newInstance()
  }
}
