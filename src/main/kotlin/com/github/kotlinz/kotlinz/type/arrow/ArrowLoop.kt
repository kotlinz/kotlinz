package com.github.kotlinz.kotlinz.type.arrow

import com.github.kotlinz.kotlinz.K3

/**
 * Feeds the second output back as the second input. Instances need a lazily evaluated
 * feedback value (e.g. MonadFix for Kleisli), so none are provided for strict data types.
 */
interface ArrowLoop<T, F> : Arrow<T, F> {
    fun <A, B, D> loop(f: K3<T, F, Pair<A, D>, Pair<B, D>>): K3<T, F, A, B>
}
