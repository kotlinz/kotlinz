package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/**
 * A [Traversable] that is also [Filterable]: elements can be dropped by an effectful function
 * while traversing.
 *
 * Laws:
 * - Identity: `wither(Identity, { Identity(f(it)) }, v) == Identity(mapFilter(f, v))`
 * - Consistency: `wither(G, f, v) == fmap(::catMaybes, traverse(G, f, v))`
 * - `filterA` consistency: `filterA(Identity, { Identity(p(it)) }, v) == Identity(filter(p, v))`
 *
 * @see Filterable
 * @see Traversable
 */
interface Witherable<T>: Traversable<T>, Filterable<T> {
  /** Runs the effectful `f` on every element, keeping the [Maybe.Just] results inside the effect. */
  fun <G, A, B> wither(applicative: Applicative<G>, f: (A) -> K1<G, Maybe<B>>, v: K1<T, A>): K1<G, K1<T, B>> =
    applicative.fmap({ t: K1<T, Maybe<B>> -> catMaybes(t) }, traverse(applicative, f, v))

  /** Keeps only the elements for which the effectful predicate `p` yields true. */
  fun <G, A> filterA(applicative: Applicative<G>, p: (A) -> K1<G, Boolean>, v: K1<T, A>): K1<G, K1<T, A>> =
    wither(applicative, { a: A -> applicative.fmap({ keep: Boolean -> if (keep) Maybe.Just(a) else Maybe.None() }, p(a)) }, v)
}
