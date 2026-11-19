package com.github.kotlinz.kotlinz.type.order

import com.github.kotlinz.kotlinz.type.eq.Eq

/**
 * A type class for total orders on values of type [A].
 *
 * [compare] returns a negative number, zero or a positive number when `a` is less than, equivalent to or
 * greater than `b`. Equivalence ([eqv]) is defined as `compare(a, b) == 0`.
 *
 * Laws (in addition to the [Eq] laws):
 * - Reflexivity: `lteqv(a, a)`
 * - Antisymmetry: `lteqv(a, b) && lteqv(b, a)` implies `eqv(a, b)`
 * - Transitivity: `lteqv(a, b) && lteqv(b, c)` implies `lteqv(a, c)`
 * - Totality: `lteqv(a, b) || lteqv(b, a)`
 *
 * ```kotlin
 * Int.order.max(1, 2)                                 // 2
 * Maybe.order(Int.order).lt(Maybe.None(), Maybe.Just(0)) // true
 * ```
 */
interface Order<A>: Eq<A> {
  /** Compares [a] with [b]: negative when `a < b`, zero when equivalent, positive when `a > b`. */
  fun compare(a: A, b: A): Int

  override fun eqv(a: A, b: A): Boolean = compare(a, b) == 0
  /** Returns true when [a] is strictly less than [b]. */
  fun lt(a: A, b: A): Boolean = compare(a, b) < 0
  /** Returns true when [a] is less than or equivalent to [b]. */
  fun lteqv(a: A, b: A): Boolean = compare(a, b) <= 0
  /** Returns true when [a] is strictly greater than [b]. */
  fun gt(a: A, b: A): Boolean = compare(a, b) > 0
  /** Returns true when [a] is greater than or equivalent to [b]. */
  fun gteqv(a: A, b: A): Boolean = compare(a, b) >= 0
  /** Returns the smaller of [a] and [b], preferring [a] on ties. */
  fun min(a: A, b: A): A = if (lteqv(a, b)) a else b
  /** Returns the larger of [a] and [b], preferring [a] on ties. */
  fun max(a: A, b: A): A = if (gteqv(a, b)) a else b
  /** Returns a [Comparator] for use with the standard library (e.g. `sortedWith`). */
  fun toComparator(): Comparator<A> = Comparator { a, b -> compare(a, b) }

  companion object {
    /** An instance that delegates to [Comparable.compareTo]. */
    fun <A: Comparable<A>> fromComparable(): Order<A> = ComparableOrder()
    /** An instance that delegates to [comparator]. */
    fun <A> fromComparator(comparator: Comparator<A>): Order<A> = by(comparator::compare)
    /** An instance built from the comparison function [f]. */
    fun <A> by(f: (A, A) -> Int): Order<A> = object: Order<A> {
      override fun compare(a: A, b: A): Int = f(a, b)
    }
    /** Lexicographic order of lists using [orderA]; a proper prefix is smaller. */
    fun <A> list(orderA: Order<A>): Order<List<A>> = object: ListOrder<A> { override val orderA = orderA }
  }
}
