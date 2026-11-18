package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

/**
 * A monad whose values can be zipped pointwise.
 *
 * Laws:
 * - Naturality: `fmap({ (a, b) -> Pair(f(a), g(b)) }, mzip(ma, mb)) == mzip(fmap(f, ma), fmap(g, mb))`
 * - Information preservation: if `fmap({ Unit }, ma) == fmap({ Unit }, mb)` then `munzip(mzip(ma, mb)) == Pair(ma, mb)`
 * - `mzipWith(m1, m2, f) == fmap({ (a, b) -> f(a, b) }, mzip(m1, m2))`
 */
interface MonadZip<T>: Monad<T> {
  /** Pairs up the values of `m1` and `m2`. */
  fun <A, B> mzip(m1: K1<T, A>, m2: K1<T, B>): K1<T, Pair<A, B>>
  /** Combines the values of `m1` and `m2` pointwise with `f`. */
  fun <A, B, C> mzipWith(m1: K1<T, A>, m2: K1<T, B>, f: (A, B) -> C): K1<T, C>
  /** Splits a computation of pairs into two computations. */
  fun <A, B> munzip(m : K1<T, Pair<A, B>>): Pair<K1<T, A>, K1<T, B>>
}
