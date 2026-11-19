package com.github.kotlinz.kotlinz.data.tracedt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Comonad

/**
 * Traced comonad transformer: a base comonad of functions `W<(M) -> A>` queried at a monoidal offset `M`.
 *
 * Use it to look "ahead" by an accumulated offset while also carrying the context of another comonad.
 * Instances: [TracedTFunctor], [TracedTComonad], [TracedTComonadTrans], [TracedTComonadTraced], all given the
 * base [Comonad] and the Monoid class of `M` (as for `Traced`).
 *
 * ```kotlin
 * val identity = object : IdentityComonad {}
 * val tracedT = TracedT(identity, StringMonoid::class.java, Identity { m: StringMonoid -> m.value.length })
 * tracedT.extract()                  // 0 (runs at mzero)
 * tracedT.trace(StringMonoid("abc")) // 3
 * ```
 *
 * @property comonad the base comonad.
 * @property type the Monoid class of `M`, used to obtain `mzero` and `op`.
 * @property run the traced functions inside the base comonad.
 */
class TracedT<M : Monoid<*, M>, W, A>(val comonad: Comonad<W>, val type: Class<M>, val run: K1<W, (M) -> A>) : K3<TracedT.T, M, W, A> {
    /** Witness type used to encode `TracedT` as a higher-kinded `K3<TracedT.T, M, W, A>`. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [TracedT]. */
        fun <M : Monoid<*, M>, W, A> narrow(v: K1<K2<T, M, W>, A>): TracedT<M, W, A> = v as TracedT<M, W, A>
    }

    /** Runs the function at the focus of the base comonad at offset `m`. */
    fun trace(m: M): A = comonad.extract(run)(m)
    /** Runs the function at an offset derived from the focused value. */
    fun traces(f: (A) -> M): A = trace(f(extract()))
    /** Drops the offset, returning the base comonad of values at `mzero`. */
    fun lower(): K1<W, A> = comonad.fmap({ g: (M) -> A -> g(Monoid.mzero(type)) }, run)

    // Comonad
    /** Maps the result of every function. */
    infix fun <B> fmap(f: (A) -> B): TracedT<M, W, B> = instance().fmap(f, this)
    /** Runs the function at the focus at `mzero`. */
    fun extract(): A = instance().extract(this)
    /** Returns a nested TracedT whose inner offset is appended to the outer one. */
    fun duplicate(): TracedT<M, W, K1<K2<T, M, W>, A>> = instance().duplicate(this)
    /** Applies [f] to the TracedT shifted by every offset at every position of the base comonad. */
    infix fun <B> extend(f: (K1<K2<T, M, W>, A>) -> B): TracedT<M, W, B> = instance().extend(f, this)

    private fun instance() = object : TracedTComonad<M, W> {
        override val comonad = this@TracedT.comonad
        override val type = this@TracedT.type
    }
}
