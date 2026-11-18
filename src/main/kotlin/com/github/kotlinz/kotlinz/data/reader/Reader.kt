package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

/**
 * A computation that reads from a shared, read-only environment `R` and produces an `A`.
 *
 * Use it for dependency injection or configuration that many functions need without threading it explicitly.
 * Has instances for Functor, Applicative, Monad, MonadReader, Distributive and Representable.
 *
 * ```kotlin
 * val length = Reader.reader<String, Int> { it.length }
 * val program = length bind { n -> Reader.ask<String>() fmap { s -> "$s has $n chars" } }
 * program.run("kotlinz") // "kotlinz has 7 chars"
 * ```
 */
class Reader<R, A>(val run: (R) -> A) : K2<Reader.T, R, A> {
    /** Higher-kinded witness tag for [Reader]. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [Reader]. */
        fun <R, A> narrow(v: K1<K1<T, R>, A>) = v as Reader<R, A>
        
        // Monad
        /** Creates a [Reader] that ignores the environment and returns [v]. */
        fun <S, A> pure(v: A): Reader<S, A> = monad<S>().pure(v)
        /** Flattens a nested [Reader], passing the same environment to both layers. */
        fun <S, A> join(v: K1<K1<T, S>, K1<K1<T, S>, A>>): Reader<S, A> = monad<S>().join(v)
        private fun <S> monad() = object: ReaderMonad<S> {}
        /** Runs a monad comprehension in [Reader]. */
        fun <S, A> fx(block: suspend MonadScope<K1<T, S>>.() -> A): Reader<S, A> = narrow(monad<S>().fx(block))

        // ApplicativeOps
        /** Lifts a unary function into [Reader] using the Applicative instance. */
        fun <S, A, B> liftA(f: (A) -> B) = applicativeOps<S>().liftA(f)
        /** Lifts a binary function into [Reader] using the Applicative instance. */
        fun <S, A, B, C> liftA2(f: (A, B) -> C) = applicativeOps<S>().liftA2(f)
        /** Lifts a ternary function into [Reader] using the Applicative instance. */
        fun <S, A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps<S>().liftA3(f)
        private fun <S> applicativeOps() = object: ReaderApplicativeOps<S> {}

        // MonadOps
        /** Lifts a unary function into [Reader] using the Monad instance. */
        fun <S, A, B> liftM(f: (A) -> B) = monadOps<S>().liftM(f)
        /** Lifts a binary function into [Reader] using the Monad instance. */
        fun <S, A, B, C> liftM2(f: (A,  B) -> C) =  monadOps<S>().liftM2(f)
        /** Lifts a ternary function into [Reader] using the Monad instance. */
        fun <S, A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps<S>().liftM3(f)
        private fun <S> monadOps() = object: ReaderMonadOps<S> {}        

        // MonadReader
        /** Returns the environment itself. */
        fun <S> ask(): Reader<S, S> = monadReader<S>().ask()
        /** Creates a [Reader] from a function of the environment. */
        fun <S, A> reader(f: (S) -> A): Reader<S, A> = monadReader<S>().reader(f)
        private fun <S> monadReader() = object: ReaderMonadReader<S> {}
    }

    // Monad
    /** Maps the result value with [f]. */
    infix fun <B> fmap(f: (A) -> B): Reader<R, B> = monad.fmap(f, this)
    /** Applies the function produced by [f] to the result value. */
    infix fun <B> ap(f: Reader<R, (A) -> B>): Reader<R, B> = monad.ap(f, this)
    /** Sequences this computation with one that depends on its result. */
    infix fun <B> bind(f: (A) -> Reader<R, B>): Reader<R, B> = monad.bind(f, this)
    private val monad = object: ReaderMonad<R> {}

    /** Runs this computation with an environment modified by [f]. */
    fun local(f: (R) -> R): Reader<R, A> = Reader { r -> run(f(r)) }
}

/** Returns the environment itself; same as [Reader.Companion.ask]. */
fun <R> ask(): Reader<R, R> = Reader { it }