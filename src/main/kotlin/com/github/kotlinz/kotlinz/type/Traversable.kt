package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/**
 * A functor that can be traversed left to right, running an applicative effect for every element
 * and collecting the results in the original shape.
 *
 * Laws:
 * - Identity: `traverse(Identity, ::Identity, v) == Identity(v)`
 * - Naturality: `t(traverse(F, f, v)) == traverse(G, t . f, v)` for every applicative transformation `t`
 * - Composition: `traverse(Compose(F, G), { Compose(fmap(g, f(it))) }, v) == Compose(fmap({ traverse(G, g, it) }, traverse(F, f, v)))`
 *
 * @see Functor
 * @see Foldable
 * @see com.github.kotlinz.kotlinz.type.applicative.Applicative
 */
interface Traversable<T>: Functor<T>, Foldable<T> {
  /** Applies the effectful `f` to each element and combines the effects with `applicative`. */
  fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<T, A>): K1<G, K1<T, B>>

  /** Turns a structure of effects inside out into an effect producing a structure. */
  fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> =
    traverse(applicative, { ga: K1<G, A> -> ga }, v)
}
