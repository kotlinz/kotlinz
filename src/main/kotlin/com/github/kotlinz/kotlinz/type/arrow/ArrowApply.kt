package com.github.kotlinz.kotlinz.type.arrow

import com.github.kotlinz.kotlinz.K3

/**
 * An arrow that can apply an arrow received as input, making it as expressive as a monad.
 *
 * Laws:
 * - `compose(app(), first(arr { x -> arr { y -> Pair(x, y) } })) == id()`
 * - `compose(app(), first(arr { h -> compose(h, g) })) == compose(app(), second(g))`
 * - `compose(app(), first(arr { g -> compose(h, g) })) == compose(h, app())`
 *
 * @see Arrow
 */
interface ArrowApply<T, F> : Arrow<T, F> {
    /** The arrow that runs its input arrow on its input value. */
    fun <A, B> app(): K3<T, F, Pair<K3<T, F, A, B>, A>, B>
}
