package com.github.kotlinz.kotlinz.data.cofreet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Comonad

/**
 * The cofree comonad transformer: `W<CofreeF<F, A, CofreeT<F, W, A>>>`, a [com.github.kotlinz.kotlinz.data.cofree.Cofree]
 * tree whose every layer is wrapped in a base comonad `W`.
 *
 * Use it for annotated, possibly infinite structures (streams, rose trees) where each node also carries the
 * context of another comonad (e.g. an environment). Tails are lazy, so [coiterT] can build infinite structures.
 * Instances: [CofreeTFunctor], [CofreeTComonad], [CofreeTComonadTrans] (all given `Functor<F>` and `Comonad<W>`).
 *
 * ```kotlin
 * val naturals = CofreeT.coiterT(identityFunctor, identityComonad, Identity(0)) { w -> Identity(Identity.narrow(w) fmap { it + 1 }) }
 * naturals.extract()                              // 0
 * Identity.narrow(naturals.unwrap()).value.extract() // 1
 * ```
 *
 * @property functor the branching functor.
 * @property comonad the base comonad.
 * @property run the first layer inside the base comonad.
 */
class CofreeT<F, W, A>(val functor: Functor<F>, val comonad: Comonad<W>, val run: K1<W, CofreeF<F, A, CofreeT<F, W, A>>>) : K3<CofreeT.T, F, W, A> {
    /** Witness type used to encode `CofreeT` as a higher-kinded `K3<CofreeT.T, F, W, A>`. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [CofreeT]. */
        fun <F, W, A> narrow(v: K1<K2<T, F, W>, A>): CofreeT<F, W, A> = v as CofreeT<F, W, A>

        /** Unfolds a structure from [seed], using [psi] to produce the next base-comonad values at every position. */
        fun <F, W, A> coiterT(functor: Functor<F>, comonad: Comonad<W>, seed: K1<W, A>, psi: (K1<W, A>) -> K1<F, K1<W, A>>): CofreeT<F, W, A> {
            val layer = { w: K1<W, A> -> CofreeF<F, A, CofreeT<F, W, A>>(comonad.extract(w)) { functor.fmap({ next: K1<W, A> -> coiterT(functor, comonad, next, psi) }, psi(w)) } }
            return CofreeT(functor, comonad, comonad.extend(layer, seed))
        }
    }

    /** Returns the sub-structure under the focused node. */
    fun unwrap(): K1<F, CofreeT<F, W, A>> = comonad.extract(run).tail
    /** Drops the tails, returning the base comonad of heads. */
    fun lower(): K1<W, A> = comonad.fmap({ layer: CofreeF<F, A, CofreeT<F, W, A>> -> layer.head }, run)

    // Comonad
    /** Maps every annotation lazily. */
    infix fun <B> fmap(f: (A) -> B): CofreeT<F, W, B> = instance().fmap(f, this)
    /** Returns the focused annotation. */
    fun extract(): A = instance().extract(this)
    /** Annotates every node with the sub-structure rooted there. */
    fun duplicate(): CofreeT<F, W, K1<K2<T, F, W>, A>> = instance().duplicate(this)
    /** Annotates every node with [f] applied to the sub-structure rooted there. */
    infix fun <B> extend(f: (K1<K2<T, F, W>, A>) -> B): CofreeT<F, W, B> = instance().extend(f, this)

    private fun instance() = object : CofreeTComonad<F, W> {
        override val functor = this@CofreeT.functor
        override val comonad = this@CofreeT.comonad
    }
}
