package com.github.kotlinz.kotlinz.type.align

import com.github.kotlinz.kotlinz.K1

/**
 * A [Semialign] whose values can also be combined on the intersection of both shapes, dropping
 * positions that exist on only one side.
 *
 * Laws:
 * - Idempotency: `zip(x, x) == fmap({ Pair(it, it) }, x)`
 * - Commutativity: `zip(x, y) == fmap({ (a, b) -> Pair(b, a) }, zip(y, x))`
 * - Associativity: `zip(x, zip(y, z)) == fmap(::assoc, zip(zip(x, y), z))`
 * - Absorption: `fmap({ it.first }, zip(x, align(x, y))) == x`
 * - Consistency: `zipWith(f, x, y) == fmap({ (a, b) -> f(a, b) }, zip(x, y))`
 *
 * @see Semialign
 */
interface Zip<T>: Semialign<T> {
  /** Combines the values present in both `a` and `b` position by position with `f`. */
  fun <A, B, C> zipWith(f: (A, B) -> C, a: K1<T, A>, b: K1<T, B>): K1<T, C>

  /** Pairs up the values present in both `a` and `b` position by position. */
  fun <A, B> zip(a: K1<T, A>, b: K1<T, B>): K1<T, Pair<A, B>> = zipWith({ x: A, y: B -> Pair(x, y) }, a, b)
}
