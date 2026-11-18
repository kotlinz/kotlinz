package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A data structure that can be reduced to a summary value by visiting its elements in order.
 *
 * Laws:
 * - `foldMap` is consistent with `foldl`: `foldMap(m, f, v) == foldl(mzero, { acc, a -> acc op f(a) }, v)`
 * - `toList(v) == foldr(emptyList(), ::cons, v)`
 *
 * @see Traversable
 * @see com.github.kotlinz.kotlinz.type.group.Monoid
 */
interface Foldable<T> {
  /** Left-associative fold: `f(f(f(initial, a1), a2), a3)`. */
  fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<T, A>): B
  /** Right-associative fold: `f(a1, f(a2, f(a3, initial)))`. */
  fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<T, A>): B

  /**
   * Maps every element to the monoid `M` and combines the results.
   * @param type the monoid class; it must have a no-arg constructor.
   */
  fun <A, M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M, v: K1<T, A>): M =
    foldl(Monoid.mzero(type), { acc: M, a: A -> Monoid.op(type, acc, f(a)) }, v)

  /** Collects the elements into a list, preserving order. */
  fun <A> toList(v: K1<T, A>): List<A> = foldr(listOf(), { a: A, acc: List<A> -> listOf(a) + acc }, v)
  /** Counts the elements. */
  fun <A> length(v: K1<T, A>): Int = foldl(0, { acc: Int, _: A -> acc + 1 }, v)
  /** Returns true when there are no elements. */
  fun <A> isEmpty(v: K1<T, A>): Boolean = length(v) == 0
}
