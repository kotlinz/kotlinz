package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/**
 * The greatest fixed point of a functor `F`: a seed of a hidden type together with a [Coalgebra]
 * that expands it one layer at a time.
 *
 * Layers are produced on demand by [project], so `Nu` can describe infinite structures such as
 * streams. Folding ([cata], [toFix], [toMu]) only terminates for finite values.
 *
 * ```kotlin
 * val naturals = Nu.unfold({ n: Int -> StreamF(n, n + 1) }, 0)
 * naturals.project(streamFunctor) // StreamF(0, <Nu seeded with 1>)
 * ```
 */
class Nu<F> private constructor(private val seed: Any?, private val coalgebra: (Any?) -> K1<F, Any?>) {
  companion object {
    /** Builds a value from a [seed] and the [coalgebra] that expands it (anamorphism). */
    @Suppress("UNCHECKED_CAST")
    fun <F, A> unfold(coalgebra: Coalgebra<F, A>, seed: A): Nu<F> = Nu(seed) { s -> coalgebra(s as A) as K1<F, Any?> }

    /** Wraps a [Fix] value, expanding it layer by layer. */
    fun <F> fromFix(fix: Fix<F>): Nu<F> = unfold({ f: Fix<F> -> f.unfix }, fix)

    /** Builds a value from one layer whose children are already [Nu] values. */
    fun <F> embed(functor: Functor<F>, layer: K1<F, Nu<F>>): Nu<F> =
      unfold({ l: K1<F, Nu<F>> -> functor.fmap({ child: Nu<F> -> child.project(functor) }, l) }, layer)
  }

  /** Expands the outermost layer, whose children are again lazily unfolded values. */
  fun project(functor: Functor<F>): K1<F, Nu<F>> = functor.fmap({ s: Any? -> Nu(s, coalgebra) }, coalgebra(seed))

  /** Folds this value with [algebra] without building an intermediate structure; diverges on infinite values. */
  fun <A> cata(functor: Functor<F>, algebra: Algebra<F, A>): A = hylo(functor, algebra, coalgebra)(seed)

  /** Converts to the explicit [Fix] representation; diverges on infinite values. */
  fun toFix(functor: Functor<F>): Fix<F> = ana(functor, coalgebra)(seed)

  /** Converts to the least fixed point; diverges on infinite values. */
  fun toMu(functor: Functor<F>): Mu<F> = Mu.fromFix(functor, toFix(functor))
}
