package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Semigroup

/**
 * A [Foldable] that always contains at least one element, so it can be reduced without an initial
 * value and mapped into a [Semigroup] (no identity element needed).
 *
 * All operations default to the non-empty [toList]; instances may override them for efficiency.
 *
 * Laws:
 * - Non-emptiness: `toList(v).isNotEmpty()`
 * - `reduceLeft(f, v) == toList(v).reduce(f)` and `reduceRight(f, v) == toList(v).reduceRight(f)`
 * - `reduceMap` is consistent with `foldMap` for monoids: `reduceMap(m, f, v) == foldMap(m, f, v)`
 *
 * @see Foldable
 * @see Traversable1
 */
interface Foldable1<T>: Foldable<T> {
  /** Left-associative reduction: `f(f(a1, a2), a3)`. */
  fun <A> reduceLeft(f: (A, A) -> A, v: K1<T, A>): A = toList(v).reduce(f)
  /** Right-associative reduction: `f(a1, f(a2, a3))`. */
  fun <A> reduceRight(f: (A, A) -> A, v: K1<T, A>): A = toList(v).reduceRight(f)

  /**
   * Maps every element to the semigroup `S` and combines the results left to right.
   * @param type the semigroup class; it must have a no-arg constructor.
   */
  fun <A, S: Semigroup<*, S>> reduceMap(type: Class<S>, f: (A) -> S, v: K1<T, A>): S =
    toList(v).map(f).reduce { acc, s -> Semigroup.op(type, acc, s) }

  /** Returns the first element. */
  fun <A> head(v: K1<T, A>): A = reduceLeft({ a, _ -> a }, v)
  /** Returns the last element. */
  fun <A> last(v: K1<T, A>): A = reduceLeft({ _, b -> b }, v)
}
