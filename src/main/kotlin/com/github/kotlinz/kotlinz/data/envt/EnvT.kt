package com.github.kotlinz.kotlinz.data.envt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.monad.Comonad

/**
 * Environment comonad transformer: a base comonad `W<A>` paired with a read-only environment `E`.
 *
 * Use it to give every position of a comonadic structure (a stream, a store, ...) access to shared
 * configuration. Instances: [EnvTFunctor], [EnvTComonad], [EnvTComonadTrans], [EnvTComonadEnv], all given
 * the base [Comonad].
 *
 * ```kotlin
 * val identity = object : IdentityComonad {}
 * val envT = EnvT(identity, 10, Identity("value"))
 * envT.extract() // "value"
 * envT.ask()     // 10
 * envT.lower()   // Identity("value")
 * ```
 *
 * @property comonad the base comonad.
 * @property env the environment.
 * @property base the wrapped base comonad value.
 */
class EnvT<E, W, A>(val comonad: Comonad<W>, val env: E, val base: K1<W, A>) : K3<EnvT.T, E, W, A> {
    /** Witness type used to encode `EnvT` as a higher-kinded `K3<EnvT.T, E, W, A>`. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [EnvT]. */
        fun <E, W, A> narrow(v: K1<K2<T, E, W>, A>): EnvT<E, W, A> = v as EnvT<E, W, A>
    }

    /** Returns the environment. */
    fun ask(): E = env
    /** Returns the environment projected through [f]. */
    fun <B> asks(f: (E) -> B): B = f(env)
    /** Returns a copy with the environment modified by [f]. */
    fun local(f: (E) -> E): EnvT<E, W, A> = EnvT(comonad, f(env), base)
    /** Drops the environment, returning the base comonad value. */
    fun lower(): K1<W, A> = base

    // Comonad
    /** Maps every value of the base comonad. */
    infix fun <B> fmap(f: (A) -> B): EnvT<E, W, B> = instance().fmap(f, this)
    /** Returns the focused value of the base comonad. */
    fun extract(): A = instance().extract(this)
    /** Wraps every position with the whole `EnvT` focused there. */
    fun duplicate(): EnvT<E, W, K1<K2<T, E, W>, A>> = instance().duplicate(this)
    /** Applies [f] at every position, keeping the environment. */
    infix fun <B> extend(f: (K1<K2<T, E, W>, A>) -> B): EnvT<E, W, B> = instance().extend(f, this)

    private fun instance() = object : EnvTComonad<E, W> { override val comonad = this@EnvT.comonad }
}
