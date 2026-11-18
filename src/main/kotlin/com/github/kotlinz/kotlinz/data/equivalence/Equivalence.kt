package com.github.kotlinz.kotlinz.data.equivalence

import com.github.kotlinz.kotlinz.K1

/**
 * An equivalence relation on `A`, wrapped as a first-class value.
 *
 * Use it to build custom equality checks compositionally, e.g. comparing values by a derived key with [contramap]
 * or combining relations on product/sum types via [EquivalenceDecidable].
 * [equiv] is expected to be reflexive, symmetric and transitive.
 *
 * Instances: Contravariant and Decidable (hence Divisible).
 *
 * ```kotlin
 * val sameLength = Equivalence.default<Int>() contramap { s: String -> s.length }
 * sameLength.equiv("abc", "xyz") // true
 * ```
 */
class Equivalence<A>(val equiv: (A, A) -> Boolean): K1<Equivalence.T, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Equivalence]. */
    fun <A> narrow(v: K1<T, A>): Equivalence<A> = v as Equivalence<A>
    /** Equivalence based on structural equality (`==`). */
    fun <A> default(): Equivalence<A> = Equivalence { a, b -> a == b }
  }

  // Contravariant
  /** Compares values of type `B` by first mapping them to `A` with [f]. */
  infix fun <B> contramap(f: (B) -> A): Equivalence<B> = contravariant.contramap(f, this)
  private val contravariant = object: EquivalenceContravariant {}
}
