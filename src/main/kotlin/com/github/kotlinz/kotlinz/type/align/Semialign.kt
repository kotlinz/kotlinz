package com.github.kotlinz.kotlinz.type.align

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.type.Functor

/**
 * A functor whose values can be combined position by position, keeping the positions that exist on
 * only one side. The shape of the result is the union of both shapes, with [Ior] marking which side
 * contributed a value.
 *
 * Laws:
 * - Idempotency: `align(x, x) == fmap({ Ior.both(it, it) }, x)`
 * - Commutativity: `align(x, y) == fmap(::swap, align(y, x))`
 * - Associativity: `align(x, align(y, z)) == fmap(::assoc, align(align(x, y), z))`
 * - Functoriality: `align(fmap(f, x), fmap(g, y)) == fmap({ it.bimap(f, g) }, align(x, y))`
 * - Consistency: `alignWith(f, x, y) == fmap(f, align(x, y))`
 *
 * @see Align
 * @see Zip
 */
interface Semialign<T>: Functor<T> {
  /** Combines `a` and `b` position by position with `f`, which receives the values present at each position. */
  fun <A, B, C> alignWith(f: (Ior<A, B>) -> C, a: K1<T, A>, b: K1<T, B>): K1<T, C>

  /** Pairs up the values of `a` and `b` position by position, keeping unmatched values. */
  fun <A, B> align(a: K1<T, A>, b: K1<T, B>): K1<T, Ior<A, B>> = alignWith({ it }, a, b)
}
