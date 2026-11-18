package com.github.kotlinz.kotlinz.type.profunctor

import com.github.kotlinz.kotlinz.K3

/**
 * A profunctor that can thread an extra value alongside its input and output (the basis of lenses).
 *
 * Laws:
 * - `first(v) == dimap(Pair::swap, Pair::swap, second(v))`
 * - `lmap({ it.first }, v) == rmap({ it.first }, first(v))`
 * - `first(first(v)) == dimap(assoc, unassoc, first(v))`
 *
 * @see Profunctor
 * @see com.github.kotlinz.kotlinz.type.arrow.Arrow
 */
interface Strong<T, F> : Profunctor<T, F> {
  /** Lifts `v` to act on the first component of a pair. */
  fun <A, B, C> first(v: K3<T, F, A, B>): K3<T, F, Pair<A, C>, Pair<B, C>>

  /** Lifts `v` to act on the second component of a pair. */
  fun <A, B, C> second(v: K3<T, F, A, B>): K3<T, F, Pair<C, A>, Pair<C, B>> =
    dimap({ p: Pair<C, A> -> p.swap() }, { p: Pair<B, C> -> p.swap() }, first<A, B, C>(v))
}

private fun <X, Y> Pair<X, Y>.swap(): Pair<Y, X> = Pair(second, first)
