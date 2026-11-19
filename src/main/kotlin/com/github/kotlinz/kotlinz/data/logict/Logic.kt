package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.syntax.MonadScope

/**
 * Helpers for `Logic`, the pure backtracking monad: [LogicT] over [Identity].
 *
 * ```kotlin
 * fun nats(n: Int): LogicT<Identity.T, Int> = Logic.pure(n) orElse Logic.defer { nats(n + 1) }
 * (nats(0) interleave Logic.of(-1, -2)).observeMany(4) // [0, -1, 1, -2]
 * ```
 */
object Logic {
    /** The `Identity` monad that `Logic` runs over. */
    val monad: IdentityMonad = object : IdentityMonad {}

    /** A search with the single result [v]. */
    fun <A> pure(v: A): LogicT<Identity.T, A> = LogicT.pure(monad, v)
    /** A search without results. */
    fun <A> empty(): LogicT<Identity.T, A> = LogicT.empty(monad)
    /** A search yielding [values] in order. */
    fun <A> of(vararg values: A): LogicT<Identity.T, A> = LogicT.fromList(monad, values.toList())
    /** Builds the search lazily when it is run; needed for recursive or infinite searches. */
    fun <A> defer(f: () -> LogicT<Identity.T, A>): LogicT<Identity.T, A> = LogicT.defer(monad, f)
    /** Builds a search with monad comprehension syntax; the block is replayed for every result. */
    fun <A> fx(block: suspend MonadScope<K1<LogicT.T, Identity.T>>.() -> A): LogicT<Identity.T, A> = LogicT.fx(monad, block)
}

/** Returns the first result of a pure search, if any. */
fun <A> LogicT<Identity.T, A>.observe(): Maybe<A> = Identity.narrow(observeT()).value
/** Returns every result of a pure search; diverges on infinite searches. */
fun <A> LogicT<Identity.T, A>.observeAll(): List<A> = Identity.narrow(observeAllT()).value
/** Returns at most [n] results of a pure search. */
fun <A> LogicT<Identity.T, A>.observeMany(n: Int): List<A> = Identity.narrow(observeManyT(n)).value
