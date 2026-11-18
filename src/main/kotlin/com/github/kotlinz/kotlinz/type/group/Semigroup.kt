package com.github.kotlinz.kotlinz.type.group

/**
 * A type with an associative binary operation.
 *
 * Instances are value classes that implement this interface with themselves as `Self` and
 * provide a public no-arg constructor, which the companion helpers use to obtain an instance
 * reflectively (e.g. `Semigroup.op(SumMonoid::class.java, a, b)`).
 *
 * Law:
 * - Associativity: `op(a, op(b, c)) == op(op(a, b), c)`
 *
 * @param A the wrapped value type.
 * @param Self the implementing type.
 * @see Monoid
 */
interface Semigroup<A, Self: Semigroup<A, Self>> {
  /** Combines `a` and `b`; the receiver is only used to select the instance. */
  fun op(a: Self, b: Self): Self

  companion object {
    /** Combines `a` and `b` using the instance created from `type`. */
    fun <A: Semigroup<*, A>> op(type: Class<A>, a: A, b: A): A = type.getConstructor().newInstance().op(a, b)
  }
}
