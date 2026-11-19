package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe

/**
 * A functor whose elements can be dropped while mapping.
 *
 * Laws:
 * - Conservation: `mapFilter({ Maybe.Just(f(it)) }, v) == fmap(f, v)`
 * - Composition: `mapFilter(g, mapFilter(f, v)) == mapFilter({ f(it) bind g }, v)`
 *
 * @see Witherable
 * @see Functor
 */
interface Filterable<T>: Functor<T> {
  /** Maps every element with `f`, keeping only the [Maybe.Just] results. */
  fun <A, B> mapFilter(f: (A) -> Maybe<B>, v: K1<T, A>): K1<T, B>

  /** Keeps only the elements satisfying `p`. */
  fun <A> filter(p: (A) -> Boolean, v: K1<T, A>): K1<T, A> =
    mapFilter({ a: A -> if (p(a)) Maybe.Just(a) else Maybe.None() }, v)

  /** Maps every element with the partial function `f`, dropping the elements it maps to `null`. */
  fun <A, B: Any> collect(f: (A) -> B?, v: K1<T, A>): K1<T, B> =
    mapFilter({ a: A -> f(a)?.let { Maybe.Just(it) } ?: Maybe.None() }, v)

  /** Keeps the values of the [Maybe.Just] elements and drops the [Maybe.None] ones. */
  fun <A> catMaybes(v: K1<T, Maybe<A>>): K1<T, A> = mapFilter({ m: Maybe<A> -> m }, v)
}
