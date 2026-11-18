package com.github.kotlinz.kotlinz.data.coproduct

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.Foldable
import com.github.kotlinz.kotlinz.type.Functor

/**
 * The sum of two type constructors: either an `F<A>` or a `G<A>`.
 *
 * Use it to combine two functors into one, e.g. to merge two instruction sets for a free monad.
 * Functor and Foldable instances are built from instances of F and G via [functor] and [foldable].
 *
 * ```kotlin
 * val left = Coproduct.left<ListK.T, Maybe.T, Int>(ListK(listOf(1, 2)))
 * val functor = Coproduct.functor(object : ListKFunctor {}, object : MaybeFunctor {})
 * functor.fmap({ x: Int -> x * 10 }, left) // Coproduct(Left(ListK [10, 20]))
 * ```
 */
data class Coproduct<F, G, A>(val run: Either<K1<F, A>, K1<G, A>>): K3<Coproduct.T, F, G, A> {
  /** Type tag used for higher-kinded encoding. */
  class T

  companion object {
    /** Downcasts the higher-kinded representation back to [Coproduct]. */
    fun <F, G, A> narrow(v: K1<K2<T, F, G>, A>): Coproduct<F, G, A> = v as Coproduct<F, G, A>

    /** Injects an `F<A>`. */
    fun <F, G, A> left(v: K1<F, A>): Coproduct<F, G, A> = Coproduct(Either.Left(v))
    /** Injects a `G<A>`. */
    fun <F, G, A> right(v: K1<G, A>): Coproduct<F, G, A> = Coproduct(Either.Right(v))

    // Instances
    /** Functor instance from Functors of F and G. */
    fun <F, G> functor(functorF: Functor<F>, functorG: Functor<G>): CoproductFunctor<F, G> =
      object: CoproductFunctor<F, G> { override val functorF = functorF; override val functorG = functorG }
    /** Foldable instance from Foldables of F and G. */
    fun <F, G> foldable(foldableF: Foldable<F>, foldableG: Foldable<G>): CoproductFoldable<F, G> =
      object: CoproductFoldable<F, G> { override val foldableF = foldableF; override val foldableG = foldableG }
  }

  /** Eliminates the coproduct by handling each side. */
  fun <B> fold(f: (K1<F, A>) -> B, g: (K1<G, A>) -> B): B = when (run) {
    is Either.Left -> f(run.value)
    is Either.Right -> g(run.value)
  }
}
