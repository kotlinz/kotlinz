package com.github.kotlinz.kotlinz.type.arrow

import com.github.kotlinz.kotlinz.K3

/**
 * Feeds the second output back as the second input. Instances need a lazily evaluated
 * feedback value (e.g. MonadFix for Kleisli), so none are provided for strict data types.
 *
 * Laws:
 * - Extension: `loop(arr(f)) == arr { a -> fix { p -> f(Pair(a, p.second)) }.first }`
 * - Left tightening: `loop(compose(f, first(h))) == compose(loop(f), h)`
 * - Right tightening: `loop(compose(first(h), f)) == compose(h, loop(f))`
 *
 * @see Arrow
 */
interface ArrowLoop<T, F> : Arrow<T, F> {
    /** Ties the knot: the `D` output of `f` is fed back as its `D` input. */
    fun <A, B, D> loop(f: K3<T, F, Pair<A, D>, Pair<B, D>>): K3<T, F, A, B>
}
