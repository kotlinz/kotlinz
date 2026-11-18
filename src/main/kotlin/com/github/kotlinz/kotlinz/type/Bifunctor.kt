package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K2

/**
 * A type constructor of two arguments that is covariant in both, such as `Either` or `Tuple2`.
 *
 * Laws:
 * - Identity: `bimap(id, id, v) == v`
 * - Composition: `bimap(f2 . f1, g2 . g1, v) == bimap(f2, g2, bimap(f1, g1, v))`
 *
 * @see Functor
 */
interface Bifunctor<T> {
  /** Maps the first type argument with `f` and the second with `g`. */
  fun <A, B, C, D> bimap(f: (A) -> C, g: (B) -> D, v: K2<T, A, B>): K2<T, C, D>
  /** Maps only the first type argument. */
  fun <A, B, C> first(f: (A) -> C, v: K2<T, A, B>): K2<T, C, B> = bimap(f, { it }, v)
  /** Maps only the second type argument. */
  fun <A, B, D> second(g: (B) -> D, v: K2<T, A, B>): K2<T, A, D> = bimap({ it }, g, v)
}
