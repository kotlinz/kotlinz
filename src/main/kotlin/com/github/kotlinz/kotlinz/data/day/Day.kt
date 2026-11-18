package com.github.kotlinz.kotlinz.data.day

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.monad.Comonad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * Day convolution of two functors: `exists B C. (F<B>, G<C>, (B, C) -> A)`.
 *
 * Use it to combine two independent effects whose results are merged by a binary function, e.g. to
 * pair two applicatives or comonads. The existential types are erased internally; use [fold] to inspect
 * the parts. Has a Functor instance for any F and G, an Applicative instance given Applicatives of both
 * ([applicative]) and a Comonad instance given Comonads of both ([comonad]).
 *
 * ```kotlin
 * val day = Day(listKOf(1, 2), listKOf(10, 20)) { b: Int, c: Int -> b + c }
 * day.dap(object : ListKApplicative {}) // ListK [11, 21, 12, 22]
 * ```
 */
@Suppress("UNCHECKED_CAST")
class Day<F, G, A> private constructor(
  internal val left: K1<F, Any?>,
  internal val right: K1<G, Any?>,
  internal val combine: (Any?, Any?) -> A
): K3<Day.T, F, G, A> {
  /** Type tag used for higher-kinded encoding. */
  class T

  /** A rank-2 function consuming the hidden parts of a [Day]. */
  interface Fold<F, G, A, R> {
    /** Handles the left part, right part and combining function. */
    operator fun <B, C> invoke(left: K1<F, B>, right: K1<G, C>, combine: (B, C) -> A): R
  }

  companion object {
    /** Downcasts the higher-kinded representation back to [Day]. */
    fun <F, G, A> narrow(v: K1<K2<T, F, G>, A>): Day<F, G, A> = v as Day<F, G, A>

    /** Creates a Day from both parts and the function combining their results. */
    operator fun <F, G, B, C, A> invoke(left: K1<F, B>, right: K1<G, C>, combine: (B, C) -> A): Day<F, G, A> =
      Day(left = left as K1<F, Any?>, right = right as K1<G, Any?>, combine = { b, c -> combine(b as B, c as C) })

    /** Pairs [fa] and [gb], combining results into a [Pair]. */
    fun <F, G, A, B> day(fa: K1<F, A>, gb: K1<G, B>): Day<F, G, Pair<A, B>> = Day(fa, gb, ::Pair)

    /** Functor instance for Day. */
    fun <F, G> functor(): DayFunctor<F, G> = object: DayFunctor<F, G> {}
    /** Applicative instance from Applicatives of F and G. */
    fun <F, G> applicative(first: Applicative<F>, second: Applicative<G>): DayApplicative<F, G> =
      DayApplicative.of(first, second)
    /** Comonad instance from Comonads of F and G. */
    fun <F, G> comonad(first: Comonad<F>, second: Comonad<G>): DayComonad<F, G> = DayComonad.of(first, second)
  }

  /** Consumes the hidden parts with [f]. */
  fun <R> fold(f: Fold<F, G, A, R>): R = f(left, right, combine)

  /** Maps over the combined result. */
  infix fun <B> fmap(f: (A) -> B): Day<F, G, B> = functor<F, G>().fmap(f, this)
  /** Swaps the two functors. */
  fun swapped(): Day<G, F, A> = Day(right, left) { c: Any?, b: Any? -> combine(b, c) }
  /** Transforms the left functor with a natural transformation. */
  fun <H> trans1(nt: NaturalTransformation<F, H>): Day<H, G, A> = Day(nt(left), right, combine)
  /** Transforms the right functor with a natural transformation. */
  fun <H> trans2(nt: NaturalTransformation<G, H>): Day<F, H, A> = Day(left, nt(right), combine)
}

/** Collapses a Day of the same functor into F using its [applicative]. */
fun <F, A> Day<F, F, A>.dap(applicative: Applicative<F>): K1<F, A> =
  applicative.ap(applicative.fmap({ b: Any? -> { c: Any? -> combine(b, c) } }, left), right)
