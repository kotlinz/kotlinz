package com.github.kotlinz.kotlinz.data.comparison

import com.github.kotlinz.kotlinz.K1

/**
 * A total ordering on A, as a first-class value wrapping a `compare` function.
 *
 * Use it to build orderings compositionally: [contramap] to compare by a projection, and
 * [ComparisonDecidable] to combine orderings lexicographically or over sums. Has instances for
 * Contravariant, Divisible and Decidable.
 *
 * ```kotlin
 * val byLength = Comparison.natural<Int>() contramap { s: String -> s.length }
 * listOf("ccc", "a", "bb").sortedWith(byLength.toComparator()) // [a, bb, ccc]
 * ```
 */
class Comparison<A>(val compare: (A, A) -> Int): K1<Comparison.T, A> {
  /** Type tag used for higher-kinded encoding. */
  class T

  companion object {
    /** Downcasts the higher-kinded representation back to [Comparison]. */
    fun <A> narrow(v: K1<T, A>): Comparison<A> = v as Comparison<A>
    /** Wraps a [Comparator]. */
    fun <A> fromComparator(comparator: Comparator<A>): Comparison<A> = Comparison(comparator::compare)
    /** The natural ordering of a [Comparable] type. */
    fun <A: Comparable<A>> natural(): Comparison<A> = Comparison { a, b -> a.compareTo(b) }
  }

  /** Converts to a [Comparator]. */
  fun toComparator(): Comparator<A> = Comparator(compare)

  // Contravariant
  /** Compares values of B by first mapping them to A with [f]. */
  infix fun <B> contramap(f: (B) -> A): Comparison<B> = contravariant.contramap(f, this)
  private val contravariant = object: ComparisonContravariant {}
}
