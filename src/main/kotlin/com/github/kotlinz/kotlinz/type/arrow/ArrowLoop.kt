package com.github.kotlinz.kotlinz.type.arrow

import com.github.kotlinz.kotlinz.K3

/**
 * Feeds the second output back as the second input.
 *
 * Kotlin is strict, so the fed-back value is passed as a thunk `() -> D`: the arrow may capture it
 * (e.g. in a lazily linked structure) but must not force it before the output is produced.
 * Instances come from value recursion, e.g. `KleisliArrowLoop` given a
 * [com.github.kotlinz.kotlinz.type.monad.MonadFix].
 *
 * Laws:
 * - Extension: `loop(arr(f)) == arr { a -> fix { p -> f(Pair(a) { p().second }) }.first }`
 * - Left tightening: `loop(compose(f, first(h))) == compose(loop(f), h)`
 * - Right tightening: `loop(compose(first(h), f)) == compose(h, loop(f))`
 *
 * @see Arrow
 */
interface ArrowLoop<T, F> : Arrow<T, F> {
    /** Ties the knot: the `D` output of `f` is fed back (lazily) as its `D` input. */
    fun <A, B, D> loop(f: K3<T, F, Pair<A, () -> D>, Pair<B, D>>): K3<T, F, A, B>
}
