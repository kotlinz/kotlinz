package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/**
 * Backtracking logic monad transformer over a base monad [F] (Kiselyov et al., "Backtracking, Interleaving,
 * and Terminating Monad Transformers"), Church-encoded as a fold over its results.
 *
 * Besides depth-first `bind` / `orElse`, it supports fair disjunction ([interleave]), fair conjunction
 * ([fairBind]), soft cut ([ifte]), pruning ([once]) and negation ([lnot]), all derived from [msplit].
 * Failure continuations are thunks, so infinite searches work when built with [defer] and consumed with
 * [observeManyT]. Deep searches are not stack-safe.
 *
 * Instances: Functor, Applicative, Monad, Alternative, MonadPlus, MonadLogic, MonadTrans, MonadIO.
 * See [Logic] for the variant over `Identity`.
 *
 * ```kotlin
 * val io = object : IOMonad {}
 * val pairs = LogicT.fx(io) { val x = LogicT.of(io, 1, 2).bind(); val y = LogicT.of(io, 3, 4).bind(); x * y }
 * IO.narrow(pairs.observeAllT()).unsafeRun() // [3, 4, 6, 8]
 * ```
 *
 * @property monad the underlying monad instance.
 */
abstract class LogicT<F, A>(val monad: Monad<F>) : K2<LogicT.T, F, A> {
    /** Witness type used to encode `LogicT` as a higher-kinded `K2<LogicT.T, F, A>`. */
    class T

    /** Folds over the results: [success] receives each value and the rest of the search, [failure] ends it. */
    abstract fun <R> run(success: (A, () -> K1<F, R>) -> K1<F, R>, failure: () -> K1<F, R>): K1<F, R>

    companion object {
        /** Downcasts a higher-kinded value back to [LogicT]. */
        fun <F, A> narrow(v: K1<K1<T, F>, A>): LogicT<F, A> = v as LogicT<F, A>

        // Monad
        /** A search with the single result [v]. */
        fun <F, A> pure(monad: Monad<F>, v: A): LogicT<F, A> = instance(monad).pure(v)
        /** Flattens a nested `LogicT`. */
        fun <F, A> join(monad: Monad<F>, v: K1<K1<T, F>, K1<K1<T, F>, A>>): LogicT<F, A> = instance(monad).join(v)
        /** Builds a search with monad comprehension syntax; the block is replayed for every result. */
        fun <F, A> fx(monad: Monad<F>, block: suspend MonadScope<K1<T, F>>.() -> A): LogicT<F, A> = narrow(instance(monad).fxReplay(block))

        // Alternative
        /** A search without results. */
        fun <F, A> empty(monad: Monad<F>): LogicT<F, A> = instance(monad).empty()
        /** A search yielding [values] in order. */
        fun <F, A> of(monad: Monad<F>, vararg values: A): LogicT<F, A> = fromList(monad, values.toList())
        /** A search yielding the elements of [values] in order. */
        fun <F, A> fromList(monad: Monad<F>, values: List<A>): LogicT<F, A> =
            values.foldRight(empty(monad)) { a, acc -> pure(monad, a) orElse acc }
        /** Builds the search lazily when it is run; needed for recursive or infinite searches. */
        fun <F, A> defer(monad: Monad<F>, f: () -> LogicT<F, A>): LogicT<F, A> = deferLogic(monad, f)

        // Transformer
        /** Lifts an `F<A>` into a search with its single result. */
        fun <F, A> lift(monad: Monad<F>, v: K1<F, A>): LogicT<F, A> = instance(monad).lift(v)
        /** Lifts an [IO] computation through the base monad's MonadIO instance. */
        fun <F, A> liftIO(monad: MonadIO<F>, v: IO<A>): LogicT<F, A> = object : LogicTMonadIO<F> { override val monad = monad }.liftIO(v)

        private fun <F> instance(monad: Monad<F>) = object : LogicTMonadLogic<F> { override val monad = monad }
    }

    // Monad
    /** Applies [f] to every result. */
    infix fun <B> fmap(f: (A) -> B): LogicT<F, B> = instance().fmap(f, this)
    /** Applies every function result of [f] to every result of this search. */
    infix fun <B> ap(f: LogicT<F, (A) -> B>): LogicT<F, B> = instance().ap(f, this)
    /** Depth-first conjunction: feeds every result to [f]. */
    infix fun <B> bind(f: (A) -> LogicT<F, B>): LogicT<F, B> = instance().bind(f, this)

    // Alternative
    /** Yields the results of this search, then those of [other]. */
    infix fun orElse(other: LogicT<F, A>): LogicT<F, A> = instance().orElse(this, other)

    // MonadLogic
    /** Splits off the first result together with the rest of the search. */
    fun msplit(): LogicT<F, Maybe<Pair<A, K1<K1<T, F>, A>>>> = instance().msplit(this)
    /** Alternates results of this search and [other]. */
    infix fun interleave(other: LogicT<F, A>): LogicT<F, A> = narrow(instance().interleave(this, other))
    /** Fair conjunction: interleaves the results produced by [f] for each result. */
    infix fun <B> fairBind(f: (A) -> LogicT<F, B>): LogicT<F, B> = narrow(instance().fairBind(f, this))
    /** Binds every result to [then], or yields [otherwise] when this search has no result. */
    fun <B> ifte(then: (A) -> LogicT<F, B>, otherwise: LogicT<F, B>): LogicT<F, B> = narrow(instance().ifte(this, then, otherwise))
    /** Keeps only the first result. */
    fun once(): LogicT<F, A> = narrow(instance().once(this))
    /** Succeeds once exactly when this search has no result. */
    fun lnot(): LogicT<F, Unit> = narrow(instance().lnot(this))

    // Observation
    /** Runs the search for its first result, if any. */
    fun observeT(): K1<F, Maybe<A>> = firstResult(this)
    /** Runs the search to completion and collects every result. */
    fun observeAllT(): K1<F, List<A>> = collectAll(this)
    /** Runs the search for at most [n] results. */
    fun observeManyT(n: Int): K1<F, List<A>> = collectMany(n, this)

    private fun instance() = object : LogicTMonadLogic<F> { override val monad = this@LogicT.monad }
}
