package com.github.kotlinz.kotlinz.data.writer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A computation producing a value [A] together with an accumulated log [W].
 *
 * The log is a [Monoid]; sequencing writers combines their logs. Use it for logging, collecting
 * metrics or building up output alongside a pure computation. [run] holds `(value, log)` and [type]
 * is the Monoid class used for `mzero`/`op`.
 *
 * Instances: [WriterFunctor], [WriterApplicative], [WriterMonad], [WriterApplicativeOps],
 * [WriterMonadOps], [WriterMonadWriter], [WriterBifunctor].
 *
 * ```kotlin
 * val type = StringMonoid::class.java
 * val w = Writer.tell(type, StringMonoid("start;")) bind { Writer.writer(type, Pair(42, StringMonoid("done;"))) }
 * w.run   // Pair(42, StringMonoid("start;done;"))
 * ```
 */
class Writer<W: Monoid<*, W>, A> (val type: Class<W>, val run: Pair<A, W>) : K2<Writer.T, W, A> {
    /** Higher-kinded witness for [Writer]. */
    class T

    companion object {
        /** Downcasts a higher-kinded value to [Writer]. */
        fun <W: Monoid<*, W>, A> narrow(v: K1<K1<T, W>, A>) = v as Writer<W, A>
        
        // Monad
        /** Wraps [v] with an empty log. */
        fun <W: Monoid<*, W>, A> pure(type: Class<W>, v: A): Writer<W, A> = monad(type).pure(v)
        /** Flattens a nested writer, combining both logs. */
        fun <W: Monoid<*, W>, A> join(type: Class<W>, v: K1<K1<T, W>, K1<K1<T, W>, A>>): Writer<W, A> = monad(type).join(v)
        private fun <W: Monoid<*, W>> monad(type: Class<W>) = object: WriterMonad<W> { override val type = type }
        /** Runs a monad comprehension block in the [Writer] monad. */
        fun <W: Monoid<*, W>, A> fx(type: Class<W>, block: suspend MonadScope<K1<T, W>>.() -> A): Writer<W, A> = narrow(monad(type).fx(block))

        // ApplicativeOps
        /** Lifts a unary function into [Writer] applicatively. */
        fun <W: Monoid<*, W>, A, B> liftA(type: Class<W>, f: (A) -> B) = applicativeOps(type).liftA(f)
        /** Lifts a binary function into [Writer] applicatively. */
        fun <W: Monoid<*, W>, A, B, C> liftA2(type: Class<W>, f: (A, B) -> C) = applicativeOps(type).liftA2(f)
        /** Lifts a ternary function into [Writer] applicatively. */
        fun <W: Monoid<*, W>, A, B, C, D> liftA3(type: Class<W>, f: (A, B, C) -> D) = applicativeOps(type).liftA3(f)
        private fun <W: Monoid<*, W>> applicativeOps(type: Class<W>) = object: WriterApplicativeOps<W> { override val type = type }

        // MonadOps
        /** Lifts a unary function into [Writer] monadically. */
        fun <W: Monoid<*, W>, A, B> liftM(type: Class<W>, f: (A) -> B) = monadOps(type).liftM(f)
        /** Lifts a binary function into [Writer] monadically. */
        fun <W: Monoid<*, W>, A, B, C> liftM2(type: Class<W>, f: (A,  B) -> C) =  monadOps(type).liftM2(f)
        /** Lifts a ternary function into [Writer] monadically. */
        fun <W: Monoid<*, W>, A, B, C, D> liftM3(type: Class<W>, f: (A, B, C) -> D) =  monadOps(type).liftM3(f)
        private fun <W: Monoid<*, W>> monadOps(type: Class<W>) = object: WriterMonadOps<W> { override val type = type }

        // MonadWriter
        /** Creates a writer from a `(value, log)` pair. */
        fun <W: Monoid<*, W>, A> writer(type: Class<W>, v: Pair<A, W>): Writer<W, A> = monadWriter(type).writer(v)
        /** Appends [w] to the log and returns [Unit]. */
        fun <W: Monoid<*, W>> tell(type: Class<W>, w: W): Writer<W, Unit> = monadWriter(type).tell(w)
        private fun <W: Monoid<*, W>> monadWriter(type: Class<W>) = object: WriterMonadWriter<W> { override val type = type }
    }

    // Monad
    /** Maps the value, keeping the log. */
    infix fun <B> fmap(f: (A) -> B): Writer<W, B> = monad.fmap(f, this)
    /** Applies the function in [f], combining logs. */
    infix fun <B> ap(f: Writer<W, (A) -> B>): Writer<W, B> = monad.ap(f, this)
    /** Sequences [f], combining logs. */
    infix fun <B> bind(f: (A) -> Writer<W, B>): Writer<W, B> = monad.bind(f, this)
    private val monad = object: WriterMonad<W> { override val type = this@Writer.type }

    // MonadWriter
    /** Exposes the log alongside the value. */
    fun listen(): Writer<W, Pair<A, W>> = monadWriter.listen(this)
    /** Transforms the log with [f]. */
    infix fun censor(f: (W) -> W): Writer<W, A> = narrow(monadWriter.censor(f, this))
    private val monadWriter = object: WriterMonadWriter<W> { override val type = this@Writer.type }

    // Bifunctor
    /** Maps both the log and the value. */
    fun <V: Monoid<*, V>, B> bimap(f: (W) -> V, g: (A) -> B): Writer<V, B> = narrow(bifunctor.bimap(f, g, this))
    /** Maps the log. */
    infix fun <V: Monoid<*, V>> first(f: (W) -> V): Writer<V, A> = narrow(bifunctor.first(f, this))
    /** Maps the value. */
    infix fun <B> second(g: (A) -> B): Writer<W, B> = narrow(bifunctor.second(g, this))
    private val bifunctor = object: WriterBifunctor {}
}

