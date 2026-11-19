package com.github.kotlinz.kotlinz.type.group

/**
 * A [CommutativeMonoid] with truncated subtraction ("monus").
 *
 * The monoid is naturally ordered by `a <= b` iff `op(a, c) == b` for some `c`, and `monus(a, b)` is the
 * smallest `c` with `a <= op(b, c)`: it removes as much of `b` from `a` as possible, never going below
 * [mzero]. Examples are natural numbers under addition (`5 ∸ 3 == 2`, `3 ∸ 5 == 0`), multisets under
 * sum and sets under union (set difference).
 *
 * Laws:
 * - `op(a, monus(b, a)) == op(b, monus(a, b))`
 * - `monus(monus(a, b), c) == monus(a, op(b, c))`
 * - `monus(a, a) == mzero()`
 * - `monus(mzero(), a) == mzero()`
 *
 * @see CommutativeMonoid
 */
interface Monus<A, Self: Monus<A, Self>>: CommutativeMonoid<A, Self> {
  /** Subtracts `b` from `a`, truncating at [mzero]. */
  fun monus(a: Self, b: Self): Self

  companion object {
    /** Subtracts `b` from `a` using the instance created from `type`. */
    fun <A: Monus<*, A>> monus(type: Class<A>, a: A, b: A): A = type.getConstructor().newInstance().monus(a, b)
  }
}
