package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

/**
 * The right Kan extension of [H] along [G]: `forall B. ((A) -> G<B>) -> H<B>`.
 *
 * Use it to obtain a Functor for free from any [H], or to reason about the universal property
 * `(K . G ~> H) ≅ (K ~> Ran<G, H>)` via [toRan] and [fromRan].
 *
 * Instances: Functor (for any `G` and `H`).
 *
 * ```kotlin
 * val catMaybes = object : FromComposition<ListK.T, Maybe.T, ListK.T> {
 *   override fun <A> invoke(v: K1<ListK.T, K1<Maybe.T, A>>): ListK<A> =
 *     ListK(ListK.narrow(v).value.flatMap { MaybeToListK(it).value })
 * }
 * val ran = Ran.narrow(toRan(object : ListKFunctor {}, catMaybes)(listKOf(1, 2, 3, 4)))
 * ran.runRan { a -> if (a % 2 == 0) Maybe.Just(a * 10) else Maybe.None() } // ListK([20, 40])
 * ```
 */
interface Ran<G, H, A>: K3<Ran.T, G, H, A> {
  /** Witness type used to encode `Ran` as a higher-kinded `K3<Ran.T, G, H, A>`. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [Ran]. */
    fun <G, H, A> narrow(v: K1<K2<T, G, H>, A>): Ran<G, H, A> = v as Ran<G, H, A>
    /** Returns the Functor instance for `Ran<G, H, *>`. */
    fun <G, H> functor(): RanFunctor<G, H> = object: RanFunctor<G, H> {}
  }

  /** Runs the extension with a continuation [f] into [G]. */
  fun <B> runRan(f: (A) -> K1<G, B>): K1<H, B>

  /** Pre-composes [f] onto the continuation. */
  infix fun <B> fmap(f: (A) -> B): Ran<G, H, B> = functor<G, H>().fmap(f, this)
}
