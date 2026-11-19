package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * Writer monad transformer: a computation in the monad [F] that also accumulates a log [W].
 *
 * [run] is `F<(value, log)>`; [monad] is the underlying monad instance and [type] the Monoid class of
 * the log. Use it to add logging to effectful computations (e.g. over `IO` or `Maybe`).
 *
 * Instances: [WriterTFunctor], [WriterTApplicative], [WriterTMonad], [WriterTMonadWriter], [WriterTMonadTrans]
 * (all require a Monad for `F` and a Monoid log) and [WriterTMFunctor].
 *
 * ```kotlin
 * val identity = object : IdentityMonad {}
 * val type = StringMonoid::class.java
 * val w = WriterT.writer(identity, type, Pair(1, StringMonoid("a"))) bind { x ->
 *   WriterT.writer(identity, type, Pair(x + 1, StringMonoid("b")))
 * }
 * w.run   // Identity of Pair(2, StringMonoid("ab"))
 * ```
 */
class WriterT<F, W: Monoid<*, W>, A>(val monad: Monad<F>, val type: Class<W>, val run: K1<F, Pair<A, W>>) : K3<WriterT.T, F, W, A> {
    /** Higher-kinded witness for [WriterT]. */
    class T

    companion object {
        /** Downcasts a higher-kinded value to [WriterT]. */
        fun <F, W: Monoid<*, W>, A> narrow(v: K1<K2<T, F, W>, A>): WriterT<F, W, A> = v as WriterT<F, W, A>

        // Monad
        /** Wraps [v] with an empty log. */
        fun <F, W: Monoid<*, W>, A> pure(monad: Monad<F>, type: Class<W>, v: A): WriterT<F, W, A> = instance(monad, type).pure(v)
        /** Flattens a nested [WriterT], combining logs. */
        fun <F, W: Monoid<*, W>, A> join(monad: Monad<F>, type: Class<W>, v: K1<K2<T, F, W>, K1<K2<T, F, W>, A>>): WriterT<F, W, A> = instance(monad, type).join(v)
        /** Runs a monad comprehension block in the [WriterT] monad. */
        fun <F, W: Monoid<*, W>, A> fx(monad: Monad<F>, type: Class<W>, block: suspend MonadScope<K2<T, F, W>>.() -> A): WriterT<F, W, A> = narrow(instance(monad, type).fx(block))

        // MonadWriter
        /** Creates a [WriterT] from a `(value, log)` pair. */
        fun <F, W: Monoid<*, W>, A> writer(monad: Monad<F>, type: Class<W>, v: Pair<A, W>): WriterT<F, W, A> = instance(monad, type).writer(v)
        /** Appends [w] to the log and returns [Unit]. */
        fun <F, W: Monoid<*, W>> tell(monad: Monad<F>, type: Class<W>, w: W): WriterT<F, W, Unit> = instance(monad, type).tell(w)

        // Transformer
        /** Lifts an `F` computation into [WriterT] with an empty log. */
        fun <F, W: Monoid<*, W>, A> lift(monad: Monad<F>, type: Class<W>, v: K1<F, A>): WriterT<F, W, A> = instance(monad, type).lift(v)

        private fun <F, W: Monoid<*, W>> instance(monad: Monad<F>, type: Class<W>) = object: WriterTMonadWriter<F, W>, WriterTMonadTrans<F, W> {
            override val monad = monad
            override val type = type
        }
    }

    // Monad
    /** Maps the value, keeping the log. */
    infix fun <B> fmap(f: (A) -> B): WriterT<F, W, B> = instance().fmap(f, this)
    /** Applies the function in [f], combining logs. */
    infix fun <B> ap(f: WriterT<F, W, (A) -> B>): WriterT<F, W, B> = instance().ap(f, this)
    /** Sequences [f], combining logs. */
    infix fun <B> bind(f: (A) -> WriterT<F, W, B>): WriterT<F, W, B> = instance().bind(f, this)

    // MonadWriter
    /** Exposes the log alongside the value. */
    fun listen(): WriterT<F, W, Pair<A, W>> = instance().listen(this)
    /** Transforms the log with [f]. */
    infix fun censor(f: (W) -> W): WriterT<F, W, A> = narrow(instance().censor(f, this))

    // MFunctor
    /** Swaps the base monad using [nt]; [target] is the monad of the new base. */
    fun <G> hoist(target: Monad<G>, nt: NaturalTransformation<F, G>): WriterT<G, W, A> {
        val type = this.type
        return object: WriterTMFunctor<F, G, W> {
            override val target = target
            override val type = type
        }.hoist(nt, this)
    }

    private fun instance() = instance(monad, type)
}
