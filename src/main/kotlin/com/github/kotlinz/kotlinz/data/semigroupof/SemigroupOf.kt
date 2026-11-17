package com.github.kotlinz.kotlinz.data.semigroupof

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Semigroup

/**
 * A first-class semigroup dictionary for values of type A.
 */
class SemigroupOf<A>(val combine: (A, A) -> A): K1<SemigroupOf.T, A> {
  class T

  companion object {
    fun <A> narrow(v: K1<T, A>): SemigroupOf<A> = v as SemigroupOf<A>
    fun <A: Semigroup<*, A>> from(type: Class<A>): SemigroupOf<A> = SemigroupOf { a, b -> Semigroup.op(type, a, b) }
  }

  // Invariant
  fun <B> imap(f: (A) -> B, g: (B) -> A): SemigroupOf<B> = invariant.imap(f, g, this)
  private val invariant = object: SemigroupOfInvariant {}
}
