package com.github.kotlinz.kotlinz.data.monoidof

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A first-class monoid dictionary for values of type A.
 */
class MonoidOf<A>(val empty: A, val combine: (A, A) -> A): K1<MonoidOf.T, A> {
  class T

  companion object {
    fun <A> narrow(v: K1<T, A>): MonoidOf<A> = v as MonoidOf<A>
    fun <A: Monoid<*, A>> from(type: Class<A>): MonoidOf<A> = MonoidOf(Monoid.mzero(type)) { a, b -> Monoid.op(type, a, b) }
  }

  // Invariant
  fun <B> imap(f: (A) -> B, g: (B) -> A): MonoidOf<B> = invariant.imap(f, g, this)
  private val invariant = object: MonoidOfInvariant {}
}
