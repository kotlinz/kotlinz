package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1

/**
 * A natural transformation out of a composition: `forall A. K<G<A>> -> H<A>`.
 *
 * Used to state the universal property of [Ran] (see [toRan] / [fromRan]).
 */
interface FromComposition<K, G, H> {
  /** Applies the transformation at type [A]. */
  operator fun <A> invoke(v: K1<K, K1<G, A>>): K1<H, A>
}

/**
 * A natural transformation into a composition: `forall A. H<A> -> F<G<A>>`.
 *
 * Used to state the universal property of [Lan] (see [toLan] / [fromLan]).
 */
interface IntoComposition<H, F, G> {
  /** Applies the transformation at type [A]. */
  operator fun <A> invoke(v: K1<H, A>): K1<F, K1<G, A>>
}
