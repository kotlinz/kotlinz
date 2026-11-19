package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A type constructor of two arguments whose values of both types can be folded into a summary,
 * such as `Either` or `Tuple2`.
 *
 * Laws:
 * - `bifoldMap` is consistent with `bifoldLeft`: `bifoldMap(m, f, g, v) == bifoldLeft(mzero, { acc, a -> acc op f(a) }, { acc, b -> acc op g(b) }, v)`
 * - `bifoldLeft` and `bifoldRight` visit the values in the same order.
 *
 * @see Foldable
 * @see Bifunctor
 */
interface Bifoldable<T> {
  /** Left-associative fold, using `f` for first-argument values and `g` for second-argument values. */
  fun <A, B, C> bifoldLeft(initial: C, f: (C, A) -> C, g: (C, B) -> C, v: K2<T, A, B>): C
  /** Right-associative fold, using `f` for first-argument values and `g` for second-argument values. */
  fun <A, B, C> bifoldRight(initial: C, f: (A, C) -> C, g: (B, C) -> C, v: K2<T, A, B>): C

  /**
   * Maps every value to the monoid `M` with `f` or `g` and combines the results.
   * @param type the monoid class; it must have a no-arg constructor.
   */
  fun <A, B, M: Monoid<*, M>> bifoldMap(type: Class<M>, f: (A) -> M, g: (B) -> M, v: K2<T, A, B>): M =
    bifoldLeft(Monoid.mzero(type), { acc: M, a: A -> Monoid.op(type, acc, f(a)) }, { acc: M, b: B -> Monoid.op(type, acc, g(b)) }, v)
}
