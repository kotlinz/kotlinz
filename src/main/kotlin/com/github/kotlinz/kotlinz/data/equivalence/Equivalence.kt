package com.github.kotlinz.kotlinz.data.equivalence

import com.github.kotlinz.kotlinz.K1

class Equivalence<A>(val equiv: (A, A) -> Boolean): K1<Equivalence.T, A> {
  class T

  companion object {
    fun <A> narrow(v: K1<T, A>): Equivalence<A> = v as Equivalence<A>
    fun <A> default(): Equivalence<A> = Equivalence { a, b -> a == b }
  }

  // Contravariant
  infix fun <B> contramap(f: (B) -> A): Equivalence<B> = contravariant.contramap(f, this)
  private val contravariant = object: EquivalenceContravariant {}
}
