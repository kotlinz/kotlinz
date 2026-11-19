package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/**
 * A [Bifunctor] that can be traversed, running an applicative effect for every value of both
 * type arguments and collecting the results in the original shape.
 *
 * Laws:
 * - Identity: `bitraverse(Identity, ::Identity, ::Identity, v) == Identity(v)`
 * - Naturality: `t(bitraverse(F, f, g, v)) == bitraverse(G, t . f, t . g, v)` for every applicative transformation `t`
 * - Composition: `bitraverse(Compose(F, G), { Compose(fmap(f2, f1(it))) }, { Compose(fmap(g2, g1(it))) }, v) ==
 *   Compose(fmap({ bitraverse(G, f2, g2, it) }, bitraverse(F, f1, g1, v)))`
 *
 * @see Bifunctor
 * @see Bifoldable
 * @see Traversable
 */
interface Bitraversable<T>: Bifunctor<T>, Bifoldable<T> {
  /** Applies `f` to first-argument values and `g` to second-argument values, combining the effects with `applicative`. */
  fun <G, A, B, C, D> bitraverse(applicative: Applicative<G>, f: (A) -> K1<G, C>, g: (B) -> K1<G, D>, v: K2<T, A, B>): K1<G, K2<T, C, D>>

  /** Turns a structure of effects on both sides inside out into an effect producing a structure. */
  fun <G, A, B> bisequence(applicative: Applicative<G>, v: K2<T, K1<G, A>, K1<G, B>>): K1<G, K2<T, A, B>> =
    bitraverse(applicative, { ga: K1<G, A> -> ga }, { gb: K1<G, B> -> gb }, v)
}
