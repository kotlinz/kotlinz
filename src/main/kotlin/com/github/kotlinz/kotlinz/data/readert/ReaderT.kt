package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.monad.Monad

/**
 * Reader monad transformer: a function `(R) -> F<A>` for an underlying monad `F`.
 *
 * Use it to add a read-only environment to another effect such as IO or Maybe.
 * Has instances for Functor, Applicative, Monad and MonadReader, all derived from the given [monad].
 *
 * ```kotlin
 * val identity = object : IdentityMonad {}
 * val program = ReaderT.ask<Identity.T, Int>(identity) bind { r -> ReaderT.pure<Identity.T, Int, Int>(identity, r * 2) }
 * program.run(21) // Identity(42)
 * ```
 */
class ReaderT<F, R, A>(val monad: Monad<F>, val run: (R) -> K1<F, A>) : K3<ReaderT.T, F, R, A> {
    /** Higher-kinded witness tag for [ReaderT]. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [ReaderT]. */
        fun <F, R, A> narrow(v: K1<K2<T, F, R>, A>): ReaderT<F, R, A> = v as ReaderT<F, R, A>

        // Monad
        /** Creates a [ReaderT] that ignores the environment and returns [v] in [monad]. */
        fun <F, R, A> pure(monad: Monad<F>, v: A): ReaderT<F, R, A> = instance<F, R>(monad).pure(v)
        /** Flattens a nested [ReaderT]. */
        fun <F, R, A> join(monad: Monad<F>, v: K1<K2<T, F, R>, K1<K2<T, F, R>, A>>): ReaderT<F, R, A> = instance<F, R>(monad).join(v)
        /** Runs a monad comprehension in [ReaderT] over [monad]. */
        fun <F, R, A> fx(monad: Monad<F>, block: suspend MonadScope<K2<T, F, R>>.() -> A): ReaderT<F, R, A> = narrow(instance<F, R>(monad).fx(block))

        // MonadReader
        /** Returns the environment itself. */
        fun <F, R> ask(monad: Monad<F>): ReaderT<F, R, R> = instance<F, R>(monad).ask()
        /** Creates a [ReaderT] from a pure function of the environment. */
        fun <F, R, A> reader(monad: Monad<F>, f: (R) -> A): ReaderT<F, R, A> = narrow(instance<F, R>(monad).reader(f))

        // Transformer
        /** Lifts an action of the underlying monad, ignoring the environment. */
        fun <F, R, A> lift(monad: Monad<F>, v: K1<F, A>): ReaderT<F, R, A> = ReaderT(monad) { _ -> v }

        private fun <F, R> instance(monad: Monad<F>) = object: ReaderTMonadReader<F, R> { override val monad = monad }
    }

    // Monad
    /** Maps the result value with [f]. */
    infix fun <B> fmap(f: (A) -> B): ReaderT<F, R, B> = instance().fmap(f, this)
    /** Applies the function produced by [f] to the result value. */
    infix fun <B> ap(f: ReaderT<F, R, (A) -> B>): ReaderT<F, R, B> = instance().ap(f, this)
    /** Sequences this computation with one that depends on its result. */
    infix fun <B> bind(f: (A) -> ReaderT<F, R, B>): ReaderT<F, R, B> = instance().bind(f, this)

    // MonadReader
    /** Runs this computation with an environment modified by [f]. */
    fun local(f: (R) -> R): ReaderT<F, R, A> = instance().local(f, this)

    private fun instance() = object: ReaderTMonadReader<F, R> { override val monad = this@ReaderT.monad }
}
