package com.github.kotlinz.kotlinz.data.compose

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.Foldable
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/**
 * The composition of two type constructors: an `F<G<A>>` treated as a single type constructor.
 *
 * Use it to work with nested effects (e.g. a list of optional values) as one layer. Functors, Applicatives
 * and Foldables compose, so instances are built from instances of F and G via [functor], [applicative],
 * [applicativeOps] and [foldable].
 *
 * ```kotlin
 * val listK = object : ListKMonad {}
 * val maybe = object : MaybeMonad {}
 * val c = Compose<ListK.T, Maybe.T, Int>(ListK(listOf(Maybe.Just(1), Maybe.None())))
 * Compose.functor(listK, maybe).fmap({ x: Int -> x * 10 }, c) // Compose(ListK [Just 10, None])
 * ```
 */
data class Compose<F, G, A>(val value: K1<F, K1<G, A>>): K3<Compose.T, F, G, A> {
  /** Type tag used for higher-kinded encoding. */
  class T

  companion object {
    /** Downcasts the higher-kinded representation back to [Compose]. */
    fun <F, G, A> narrow(v: K1<K2<T, F, G>, A>): Compose<F, G, A> = v as Compose<F, G, A>

    // Instances
    /** Functor instance from Functors of F and G. */
    fun <F, G> functor(functorF: Functor<F>, functorG: Functor<G>): ComposeFunctor<F, G> =
      object: ComposeFunctor<F, G> { override val functorF = functorF; override val functorG = functorG }
    /** Applicative instance from Applicatives of F and G. */
    fun <F, G> applicative(functorF: Applicative<F>, functorG: Applicative<G>): ComposeApplicative<F, G> = applicativeOps(functorF, functorG)
    /** ApplicativeOps instance from Applicatives of F and G. */
    fun <F, G> applicativeOps(functorF: Applicative<F>, functorG: Applicative<G>): ComposeApplicativeOps<F, G> =
      object: ComposeApplicativeOps<F, G> { override val functorF = functorF; override val functorG = functorG }
    /** Foldable instance from Foldables of F and G. */
    fun <F, G> foldable(foldableF: Foldable<F>, foldableG: Foldable<G>): ComposeFoldable<F, G> =
      object: ComposeFoldable<F, G> { override val foldableF = foldableF; override val foldableG = foldableG }

    // Applicative
    /** Wraps a pure value in both layers. */
    fun <F, G, A> pure(functorF: Applicative<F>, functorG: Applicative<G>, v: A): Compose<F, G, A> = applicative(functorF, functorG).pure(v)
  }
}
