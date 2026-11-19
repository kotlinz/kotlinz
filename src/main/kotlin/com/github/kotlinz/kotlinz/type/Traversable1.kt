package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Apply

/**
 * A non-empty [Traversable]: because there is always a first element, traversals only need an
 * [Apply] (no `pure`), so effects such as non-empty validations or semigroup-only writers work.
 *
 * Laws (the [Traversable] laws with [Apply] in place of Applicative), plus:
 * - Consistency: `traverse1(F, f, v) == traverse(F, f, v)` for every Applicative `F`
 * - Identity: `traverse1(Identity, ::Identity, v) == Identity(v)`
 * - Naturality: `t(traverse1(F, f, v)) == traverse1(G, t . f, v)` for every apply transformation `t`
 *
 * @see Traversable
 * @see Foldable1
 * @see com.github.kotlinz.kotlinz.type.applicative.Apply
 */
interface Traversable1<T>: Traversable<T>, Foldable1<T> {
  /** Applies the effectful `f` to each element, left to right, and combines the effects with `apply`. */
  fun <G, A, B> traverse1(apply: Apply<G>, f: (A) -> K1<G, B>, v: K1<T, A>): K1<G, K1<T, B>>

  /** Turns a non-empty structure of effects inside out using only [Apply]. */
  fun <G, A> sequence1(apply: Apply<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> =
    traverse1(apply, { ga: K1<G, A> -> ga }, v)
}
