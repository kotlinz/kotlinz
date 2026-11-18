package com.github.kotlinz.kotlinz.type.category

import com.github.kotlinz.kotlinz.K3

/**
 * A [Compose] with identity arrows.
 *
 * Laws:
 * - Left identity: `compose(id(), f) == f`
 * - Right identity: `compose(f, id()) == f`
 *
 * @see com.github.kotlinz.kotlinz.type.arrow.Arrow
 */
interface Category<T, F> : Compose<T, F> {
    /** The identity arrow on `A`. */
    fun <A> id(): K3<T, F, A, A>
}
