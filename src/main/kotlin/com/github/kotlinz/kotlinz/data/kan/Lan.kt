package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

/**
 * The left Kan extension of [H] along [G]: `exists B. ((G<B>) -> A, H<B>)`.
 *
 * It pairs a hidden `H<B>` with a way to turn `G<B>` into `A`. Use it to obtain a Functor for free
 * from any [H], or to reason about the universal property `(Lan<G, H> ~> F) ≅ (H ~> F . G)` via
 * [toLan] and [fromLan]. The existential `B` is erased internally; inspect it with [fold].
 *
 * Instances: Functor (for any `G` and `H`).
 *
 * ```kotlin
 * val lan = Lan({ m: K1<Maybe.T, Int> -> Maybe.narrow(m).getOrElse(0) * 2 }, listKOf(1, 2, 3))
 * val wrapJust = object : IntoComposition<ListK.T, ListK.T, Maybe.T> {
 *   override fun <A> invoke(v: K1<ListK.T, A>): ListK<K1<Maybe.T, A>> = ListK(ListK.narrow(v).value.map { Maybe.Just(it) })
 * }
 * toLan(object : ListKFunctor {}, wrapJust)(lan fmap { it + 1 }) // ListK([3, 5, 7])
 * ```
 */
@Suppress("UNCHECKED_CAST")
class Lan<G, H, A> private constructor(
  internal val lower: (K1<G, Any?>) -> A,
  internal val value: K1<H, Any?>
): K3<Lan.T, G, H, A> {
  /** Witness type used to encode `Lan` as a higher-kinded `K3<Lan.T, G, H, A>`. */
  class T

  /** A rank-2 eliminator that can observe the hidden type of a [Lan]. */
  interface Fold<G, H, A, R> {
    /** Consumes the lowering function and the hidden value for any `B`. */
    operator fun <B> invoke(f: (K1<G, B>) -> A, v: K1<H, B>): R
  }

  companion object {
    /** Downcasts a higher-kinded value back to [Lan]. */
    fun <G, H, A> narrow(v: K1<K2<T, G, H>, A>): Lan<G, H, A> = v as Lan<G, H, A>
    /** Returns the Functor instance for `Lan<G, H, *>`. */
    fun <G, H> functor(): LanFunctor<G, H> = object: LanFunctor<G, H> {}

    /** Builds a `Lan` from a lowering function [f] and a hidden value [v]. */
    operator fun <G, H, B, A> invoke(f: (K1<G, B>) -> A, v: K1<H, B>): Lan<G, H, A> =
      Lan(lower = { gb -> f(gb as K1<G, B>) }, value = v as K1<H, Any?>)
  }

  /** Eliminates this `Lan` with [f], which receives the hidden components. */
  fun <R> fold(f: Fold<G, H, A, R>): R = f(lower, value)

  /** Post-composes [f] onto the lowering function. */
  infix fun <B> fmap(f: (A) -> B): Lan<G, H, B> = functor<G, H>().fmap(f, this)
}
