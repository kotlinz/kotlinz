package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

/**
 * A lazy, deferred computation `() -> A`.
 *
 * Wraps a zero-argument function so it can be used with type class abstractions.
 * The thunk is re-run on every [invoke]; results are not memoized (use
 * [com.github.kotlinz.kotlinz.data.eval.Eval] for memoization).
 *
 * Instances: [Function0Functor], [Function0Applicative], [Function0ApplicativeOps],
 * [Function0Monad], [Function0MonadOps], [Function0Copointed], [Function0Comonad].
 *
 * ```kotlin
 * val m = Function0 { 3 }
 * (m bind { x -> Function0 { x + 1 } })() // 4
 * Function0.fx { m.bind() + Function0 { 4 }.bind() }() // 7
 * ```
 */
class Function0<A>(val run: () -> A): K1<Function0.T, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Function0]. */
    fun <A> narrow(v: K1<T, A>): Function0<A> = v as Function0<A>

    // Monad
    /** Wraps a value into a thunk that returns it. */
    fun <A> pure(v: A): Function0<A> = monad.pure(v)
    /** Flattens a nested thunk. */
    fun <A> join(v: K1<T, K1<T, A>>): Function0<A> = monad.join(v)
    private val monad = object: Function0Monad {}
    /** Builds a thunk with monad comprehension syntax. */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Function0<A> = narrow(monad.fx(block))

    // ApplicativeOps
    /** Lifts a unary function to operate on [Function0] values. */
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    /** Lifts a binary function to operate on [Function0] values. */
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    /** Lifts a ternary function to operate on [Function0] values. */
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: Function0ApplicativeOps {}

    // MonadOps
    /** Lifts a unary function using monadic bind. */
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    /** Lifts a binary function using monadic bind. */
    fun <A, B, C> liftM2(f: (A, B) -> C) = monadOps.liftM2(f)
    /** Lifts a ternary function using monadic bind. */
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) = monadOps.liftM3(f)
    private val monadOps = object: Function0MonadOps {}
  }

  /** Runs the computation. */
  operator fun invoke(): A = run()

  // Monad
  /** Maps the result lazily. */
  infix fun <B> fmap(f: (A) -> B): Function0<B> = monad.fmap(f, this)
  /** Applies the function produced by [f] to the result. */
  infix fun <B> ap(f: Function0<(A) -> B>): Function0<B> = monad.ap(f, this)
  /** Sequences this computation with [f] lazily. */
  infix fun <B> bind(f: (A) -> Function0<B>): Function0<B> = monad.bind(f, this)
  private val monad = object: Function0Monad {}

  // Comonad
  /** Runs the computation and returns its result. */
  fun extract(): A = comonad.extract(this)
  /** Wraps this computation in another thunk. */
  fun duplicate(): Function0<K1<T, A>> = comonad.duplicate(this)
  /** Lazily applies [f] to this whole computation. */
  fun <B> extend(f: (K1<T, A>) -> B): Function0<B> = comonad.extend(f, this)
  private val comonad = object: Function0Comonad {}
}
