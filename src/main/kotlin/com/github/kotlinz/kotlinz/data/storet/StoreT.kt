package com.github.kotlinz.kotlinz.data.storet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Comonad

/**
 * Store comonad transformer: a base comonad of lookup functions `W<(S) -> A>` together with a current position `S`.
 *
 * Use it when a position-indexed computation (a grid, a timeline) should also carry the context of another
 * comonad. Instances: [StoreTFunctor], [StoreTComonad], [StoreTComonadTrans], [StoreTComonadStore], all given
 * the base [Comonad].
 *
 * ```kotlin
 * val identity = object : IdentityComonad {}
 * val storeT = StoreT(identity, Identity { s: Int -> s * 10 }, 2)
 * storeT.extract()         // 20
 * storeT.seek(4).extract() // 40
 * storeT.lower()           // Identity(20)
 * ```
 *
 * @property comonad the base comonad.
 * @property run the lookup functions inside the base comonad.
 * @property pos the current position.
 */
class StoreT<S, W, A>(val comonad: Comonad<W>, val run: K1<W, (S) -> A>, val pos: S) : K3<StoreT.T, S, W, A> {
    /** Witness type used to encode `StoreT` as a higher-kinded `K3<StoreT.T, S, W, A>`. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [StoreT]. */
        fun <S, W, A> narrow(v: K1<K2<T, S, W>, A>): StoreT<S, W, A> = v as StoreT<S, W, A>
    }

    /** Looks up the value at the absolute position `s`. */
    fun peek(s: S): A = comonad.extract(run)(s)
    /** Looks up the value at a position relative to the current one. */
    fun peeks(f: (S) -> S): A = peek(f(pos))
    /** Moves to the absolute position `s`. */
    fun seek(s: S): StoreT<S, W, A> = StoreT(comonad, run, s)
    /** Moves to a position relative to the current one. */
    fun seeks(f: (S) -> S): StoreT<S, W, A> = seek(f(pos))
    /** Looks up values at every position produced by `f`, inside functor `F`. */
    fun <F> experiment(functor: Functor<F>, f: (S) -> K1<F, S>): K1<F, A> = functor.fmap({ s: S -> peek(s) }, f(pos))
    /** Drops the position, returning the base comonad of values at the current position. */
    fun lower(): K1<W, A> = comonad.fmap({ g: (S) -> A -> g(pos) }, run)

    // Comonad
    /** Maps every value of the store. */
    infix fun <B> fmap(f: (A) -> B): StoreT<S, W, B> = instance().fmap(f, this)
    /** Returns the value at the current position. */
    fun extract(): A = instance().extract(this)
    /** Returns a store of stores, each focused on its own position. */
    fun duplicate(): StoreT<S, W, K1<K2<T, S, W>, A>> = instance().duplicate(this)
    /** Applies [f] to the store focused at every position. */
    infix fun <B> extend(f: (K1<K2<T, S, W>, A>) -> B): StoreT<S, W, B> = instance().extend(f, this)

    private fun instance() = object : StoreTComonad<S, W> { override val comonad = this@StoreT.comonad }
}
