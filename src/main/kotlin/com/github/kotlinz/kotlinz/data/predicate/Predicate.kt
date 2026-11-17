package com.github.kotlinz.kotlinz.data.predicate

import com.github.kotlinz.kotlinz.K1

class Predicate<A>(val test: (A) -> Boolean): K1<Predicate.T, A> {
  class T

  companion object {
    fun <A> narrow(v: K1<T, A>): Predicate<A> = v as Predicate<A>
  }

  // Contravariant
  infix fun <B> contramap(f: (B) -> A): Predicate<B> = contravariant.contramap(f, this)
  private val contravariant = object: PredicateContravariant {}
}
