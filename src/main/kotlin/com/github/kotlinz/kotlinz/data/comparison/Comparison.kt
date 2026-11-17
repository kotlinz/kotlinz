package com.github.kotlinz.kotlinz.data.comparison

import com.github.kotlinz.kotlinz.K1

class Comparison<A>(val compare: (A, A) -> Int): K1<Comparison.T, A> {
  class T

  companion object {
    fun <A> narrow(v: K1<T, A>): Comparison<A> = v as Comparison<A>
    fun <A> fromComparator(comparator: Comparator<A>): Comparison<A> = Comparison(comparator::compare)
    fun <A: Comparable<A>> natural(): Comparison<A> = Comparison { a, b -> a.compareTo(b) }
  }

  fun toComparator(): Comparator<A> = Comparator(compare)

  // Contravariant
  infix fun <B> contramap(f: (B) -> A): Comparison<B> = contravariant.contramap(f, this)
  private val contravariant = object: ComparisonContravariant {}
}
