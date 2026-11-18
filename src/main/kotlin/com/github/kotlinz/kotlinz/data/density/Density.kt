package com.github.kotlinz.kotlinz.data.density

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Comonad

/**
 * The density comonad: an `F<B>` paired with a function `(F<B>) -> A`, existentially quantified over B.
 *
 * It is the comonadic dual of [com.github.kotlinz.kotlinz.data.codensity.Codensity]: [lift] a comonadic
 * value, build a chain of [extend]s cheaply, then [lower] back. Has instances for Functor, Copointed and
 * Comonad that require nothing of F; only [lift] and [lower] need a Comonad of F.
 *
 * ```kotlin
 * val nel = object : NonEmptyListComonad {}
 * val density = Density.lift(nel, NonEmptyList(1, listOf(2, 3)))
 * density.extract() // 1
 * density.lower(nel) // NonEmptyList(1, [2, 3])
 * ```
 */
@Suppress("UNCHECKED_CAST")
class Density<F, A> private constructor(val f: (K1<F, Any?>) -> A, val fb: K1<F, Any?>): K2<Density.T, F, A> {
  /** Type tag used for higher-kinded encoding. */
  class T

  companion object {
    /** Downcasts the higher-kinded representation back to [Density]. */
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Density<F, A> = v as Density<F, A>

    /** Pairs the observation [f] with the focus [fb]. */
    fun <F, B, A> of(f: (K1<F, B>) -> A, fb: K1<F, B>): Density<F, A> = Density(f as (K1<F, Any?>) -> A, fb as K1<F, Any?>)

    /** Lifts a value of the underlying [comonad] into Density. */
    fun <F, A> lift(comonad: Comonad<F>, fa: K1<F, A>): Density<F, A> = of({ w: K1<F, A> -> comonad.extract(w) }, fa)
  }

  /** Runs the accumulated observation back into F with [comonad]'s `extend`. */
  fun lower(comonad: Comonad<F>): K1<F, A> = comonad.extend(f, fb)

  // Comonad
  /** Maps over the observed value. */
  infix fun <B> fmap(g: (A) -> B): Density<F, B> = comonad.fmap(g, this)
  /** Observes the current value. */
  fun extract(): A = comonad.extract(this)
  /** Wraps each context in another Density layer. */
  fun duplicate(): Density<F, K1<K1<T, F>, A>> = comonad.duplicate(this)
  /** Extends a context-dependent computation [g] over the structure. */
  infix fun <B> extend(g: (K1<K1<T, F>, A>) -> B): Density<F, B> = comonad.extend(g, this)
  private val comonad get() = object: DensityComonad<F> {}
}
