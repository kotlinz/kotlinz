package com.github.kotlinz.kotlinz.data.freet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * One layer of a [FreeT] program: either a finished result or a suspended instruction of `F`
 * whose continuations are of type `B`.
 */
sealed class FreeF<F, A, B> {
    /** A finished program returning [value]. */
    data class Pure<F, A, B>(val value: A) : FreeF<F, A, B>()
    /** A suspended instruction [next] holding the continuations. */
    data class Free<F, A, B>(val next: K1<F, B>) : FreeF<F, A, B>()

    /** Maps the continuations with [f], leaving a finished result untouched. */
    fun <C> mapNext(functor: Functor<F>, f: (B) -> C): FreeF<F, A, C> = when (this) {
        is Pure -> Pure(value)
        is Free -> Free(functor.fmap(f, next))
    }

    /** Changes the instruction set with [nt]. */
    fun <G> transform(nt: NaturalTransformation<F, G>): FreeF<G, A, B> = when (this) {
        is Pure -> Pure(value)
        is Free -> Free(nt(next))
    }
}
