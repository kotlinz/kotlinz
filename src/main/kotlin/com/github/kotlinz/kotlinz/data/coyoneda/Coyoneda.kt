package com.github.kotlinz.kotlinz.data.coyoneda

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * The free functor (co-Yoneda lemma): an `F<B>` paired with a function `(B) -> A`, existentially quantified over B.
 *
 * Use it to get a Functor for any type constructor F without an instance, or to fuse a chain of
 * [fmap]s into a single pass that runs on [lower]. Has a Functor instance that requires nothing of F;
 * only [lower] needs a Functor of F.
 *
 * ```kotlin
 * val mapped = Coyoneda.lift(listKOf(1, 2)) fmap { it + 1 } fmap { it * 2 }
 * mapped.lower(object : ListKFunctor {}) // ListK [4, 6]
 * ```
 */
@Suppress("UNCHECKED_CAST")
class Coyoneda<F, A> private constructor(val fb: K1<F, Any?>, val f: (Any?) -> A): K2<Coyoneda.T, F, A> {
  /** Type tag used for higher-kinded encoding. */
  class T

  companion object {
    /** Downcasts the higher-kinded representation back to [Coyoneda]. */
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Coyoneda<F, A> = v as Coyoneda<F, A>

    /** Pairs [fb] with the pending function [f]. */
    fun <F, B, A> of(fb: K1<F, B>, f: (B) -> A): Coyoneda<F, A> = Coyoneda(fb as K1<F, Any?>, f as (Any?) -> A)

    /** Lifts an `F<A>` with the identity function. */
    fun <F, A> lift(fa: K1<F, A>): Coyoneda<F, A> = of(fa) { it }
  }

  /** Applies the accumulated function with a single `fmap` of [functor]. */
  fun lower(functor: Functor<F>): K1<F, A> = functor.fmap(f, fb)

  /** Changes the underlying type constructor with a natural transformation. */
  fun <G> hoist(nt: NaturalTransformation<F, G>): Coyoneda<G, A> = of(nt(fb), f)

  // Functor
  /** Composes [g] onto the pending function without touching the underlying value. */
  infix fun <B> fmap(g: (A) -> B): Coyoneda<F, B> = functor.fmap(g, this)
  private val functor get() = object: CoyonedaFunctor<F> {}
}
