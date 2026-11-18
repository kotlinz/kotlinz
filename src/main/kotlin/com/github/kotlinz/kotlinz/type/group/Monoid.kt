package com.github.kotlinz.kotlinz.type.group

/**
 * A [Semigroup] with an identity element.
 *
 * Laws:
 * - Left identity: `op(mzero(), a) == a`
 * - Right identity: `op(a, mzero()) == a`
 *
 * @see Semigroup
 * @see Group
 */
interface Monoid<A, Self: Monoid<A, Self>>: Semigroup<A, Self> {
  /** The identity element of [op]. */
  fun mzero(): Self

  companion object {
    /** Combines `a` and `b` using the instance created from `type`. */
    fun <A: Monoid<*, A>> op(type: Class<A>, a: A, b: A): A = type.getConstructor().newInstance().op(a, b)
    /** Returns the identity element of the monoid `type`. */
    fun <A: Monoid<*, A>> mzero(type: Class<A>): A = type.getConstructor().newInstance().mzero()
  }
}
