package com.github.kotlinz.kotlinz.type.category

import com.github.kotlinz.kotlinz.K3

/**
 * A semigroupoid: arrows that can be composed associatively.
 *
 * Arrows are encoded as `K3<T, F, A, B>` (an arrow from `A` to `B`), where `F` is an extra
 * parameter of the arrow type such as the monad of a Kleisli arrow (`Unit` when unused).
 *
 * Law:
 * - Associativity: `compose(f, compose(g, h)) == compose(compose(f, g), h)`
 *
 * @see Category
 */
interface Compose<T, F> {
    /** Composes right to left: the result runs `g` and then `f`. */
    fun <A, B, C> compose(f: K3<T, F, B, C>, g: K3<T, F, A, B>): K3<T, F, A, C>
}
