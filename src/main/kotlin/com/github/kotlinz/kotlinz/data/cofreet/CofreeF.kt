package com.github.kotlinz.kotlinz.data.cofreet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/**
 * One layer of a [CofreeT]: a [head] annotating an `F`-shaped [tail] of further nodes of type `B`.
 * The tail is evaluated lazily, so infinite structures can be described.
 */
class CofreeF<F, A, B>(val head: A, tail: () -> K1<F, B>) {
    /** The lazily evaluated sub-structure. */
    val tail: K1<F, B> by lazy(tail)

    /** Maps the head with [f] and every tail node with [g], keeping the tail lazy. */
    fun <C, D> bimap(functor: Functor<F>, f: (A) -> C, g: (B) -> D): CofreeF<F, C, D> =
        CofreeF(f(head)) { functor.fmap(g, tail) }
}
