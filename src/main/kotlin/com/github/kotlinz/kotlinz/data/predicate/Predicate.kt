package com.github.kotlinz.kotlinz.data.predicate

import com.github.kotlinz.kotlinz.K1

/**
 * A boolean test on values of type `A`.
 *
 * Use it to build predicates compositionally: [contramap] adapts the input,
 * and [PredicateDecidable] combines predicates with `&&` (divide) or by branching (choose).
 * Has instances for Contravariant, Divisible and Decidable.
 *
 * ```kotlin
 * val isLong = Predicate { n: Int -> n > 3 } contramap { s: String -> s.length }
 * isLong.test("kotlinz") // true
 * ```
 */
class Predicate<A>(val test: (A) -> Boolean): K1<Predicate.T, A> {
  /** Higher-kinded witness tag for [Predicate]. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [Predicate]. */
    fun <A> narrow(v: K1<T, A>): Predicate<A> = v as Predicate<A>
  }

  // Contravariant
  /** Tests the value produced by [f] instead of the original input. */
  infix fun <B> contramap(f: (B) -> A): Predicate<B> = contravariant.contramap(f, this)
  private val contravariant = object: PredicateContravariant {}
}
