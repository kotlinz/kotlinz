package com.github.kotlinz.kotlinz.type.group

/**
 * A [Monoid] in which every element has an inverse.
 *
 * Laws:
 * - `op(a, inverse(a)) == mzero()`
 * - `op(inverse(a), a) == mzero()`
 *
 * @see Monoid
 */
interface Group<A, Self: Group<A, Self>>: Monoid<A, Self> {
  /** Returns the inverse of `a`. */
  fun inverse(a: Self): Self

  companion object {
    /** Returns the inverse of `a` using the instance created from `type`. */
    fun <A: Group<*, A>> inverse(type: Class<A>, a: A): A = type.getConstructor().newInstance().inverse(a)
  }
}
