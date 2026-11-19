package com.github.kotlinz.kotlinz.data.fix

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.data.fix.cata as cataScheme

/**
 * The least fixed point of a functor `F`, Church-encoded as its own fold: a value of `Mu<F>` is
 * anything that can be consumed by every [Algebra] of `F`.
 *
 * Folding never needs a functor and always terminates for values built from finite data.
 * Convert from and to [Fix] with [fromFix] / [toFix], and to the greatest fixed point with [toNu].
 *
 * ```kotlin
 * val three = Mu.ana(natFunctor, toNat, 3)
 * three.cata(toInt)                  // 3
 * three.toFix() == Fix.ana(natFunctor, toNat, 3) // true
 * ```
 */
abstract class Mu<F> {
  companion object {
    /** Wraps a [Fix] value; folds delegate to [cata] over it. */
    fun <F> fromFix(functor: Functor<F>, fix: Fix<F>): Mu<F> = object: Mu<F>() {
      override fun <A> cata(algebra: Algebra<F, A>): A = cataScheme(functor, algebra)(fix)
    }

    /** Builds a value from one layer whose children are already [Mu] values. */
    fun <F> embed(functor: Functor<F>, layer: K1<F, Mu<F>>): Mu<F> = object: Mu<F>() {
      override fun <A> cata(algebra: Algebra<F, A>): A = algebra(functor.fmap({ child: Mu<F> -> child.cata(algebra) }, layer))
    }

    /** Unfolds [seed] eagerly with [coalgebra]; the unfold must terminate. */
    fun <F, A> ana(functor: Functor<F>, coalgebra: Coalgebra<F, A>, seed: A): Mu<F> =
      fromFix(functor, Fix.ana(functor, coalgebra, seed))
  }

  /** Folds this value with [algebra] (catamorphism). */
  abstract fun <A> cata(algebra: Algebra<F, A>): A

  /** Exposes the outermost layer. */
  fun project(functor: Functor<F>): K1<F, Mu<F>> =
    cata { layer: K1<F, K1<F, Mu<F>>> -> functor.fmap({ inner: K1<F, Mu<F>> -> embed(functor, inner) }, layer) }

  /** Converts to the explicit [Fix] representation. */
  fun toFix(): Fix<F> = cata { layer: K1<F, Fix<F>> -> Fix(layer) }

  /** Converts to the greatest fixed point. */
  fun toNu(functor: Functor<F>): Nu<F> = Nu.unfold({ mu: Mu<F> -> mu.project(functor) }, this)
}
