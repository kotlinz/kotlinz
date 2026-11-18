package com.github.kotlinz.kotlinz.data.product

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.Foldable
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/**
 * The product of two functors: a pair of `F<A>` and `G<A>` sharing the same element type.
 *
 * Use it to run two independent structures or effects side by side, e.g. a list and a [Maybe][com.github.kotlinz.kotlinz.data.maybe.Maybe].
 * Instances are built from the component instances via [functor], [applicative], [applicativeOps] and [foldable]
 * (Functor, Applicative and Foldable).
 *
 * ```kotlin
 * val listK = object : ListKMonad {}
 * val maybe = object : MaybeMonad {}
 * val p = Product<ListK.T, Maybe.T, Int>(ListK(listOf(1, 2)), Maybe.Just(3))
 * Product.functor(listK, maybe).fmap({ x: Int -> x * 10 }, p) // Product(ListK(listOf(10, 20)), Maybe.Just(30))
 * ```
 */
data class Product<F, G, A>(val first: K1<F, A>, val second: K1<G, A>): K3<Product.T, F, G, A> {
  /** Higher-kinded witness tag for [Product]. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [Product]. */
    fun <F, G, A> narrow(v: K1<K2<T, F, G>, A>): Product<F, G, A> = v as Product<F, G, A>

    // Instances
    /** Functor instance for [Product] built from the component functors. */
    fun <F, G> functor(functorF: Functor<F>, functorG: Functor<G>): ProductFunctor<F, G> =
      object: ProductFunctor<F, G> { override val functorF = functorF; override val functorG = functorG }
    /** Applicative instance for [Product] built from the component applicatives. */
    fun <F, G> applicative(functorF: Applicative<F>, functorG: Applicative<G>): ProductApplicative<F, G> = applicativeOps(functorF, functorG)
    /** ApplicativeOps instance for [Product] built from the component applicatives. */
    fun <F, G> applicativeOps(functorF: Applicative<F>, functorG: Applicative<G>): ProductApplicativeOps<F, G> =
      object: ProductApplicativeOps<F, G> { override val functorF = functorF; override val functorG = functorG }
    /** Foldable instance for [Product]; folds the first component, then the second. */
    fun <F, G> foldable(foldableF: Foldable<F>, foldableG: Foldable<G>): ProductFoldable<F, G> =
      object: ProductFoldable<F, G> { override val foldableF = foldableF; override val foldableG = foldableG }

    // Applicative
    /** Lifts [v] into both components. */
    fun <F, G, A> pure(functorF: Applicative<F>, functorG: Applicative<G>, v: A): Product<F, G, A> = applicative(functorF, functorG).pure(v)
  }
}
