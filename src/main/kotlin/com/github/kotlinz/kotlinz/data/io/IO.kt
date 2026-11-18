package com.github.kotlinz.kotlinz.data.io

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

/**
 * A suspended side-effecting computation.
 *
 * Describes an effect without performing it; nothing happens until [unsafeRun] is called,
 * and each call re-runs the effect. Exceptions thrown inside are surfaced through the
 * [IOMonadError] instance. Not stack-safe: very deep bind chains may overflow the stack.
 *
 * Instances: [IOFunctor], [IOApplicative], [IOApplicativeOps], [IOMonad], [IOMonadOps],
 * [IOMonadError] (with [Throwable] as the error type).
 *
 * ```kotlin
 * var count = 0
 * val io = IO { count += 1 } fmap { count }
 * io.unsafeRun() // 1
 * IO.raiseError<Int>(IllegalStateException()) handleError { -1 } // IO producing -1
 * ```
 */
class IO<A>(private val run: () -> A): K1<IO.T, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [IO]. */
    fun <A> narrow(v: K1<T, A>): IO<A> = v as IO<A>

    // Monad
    /** Wraps a pure value into [IO] without side effects. */
    fun <A> pure(v: A): IO<A> = monad.pure(v)
    /** Flattens a nested [IO]. */
    fun <A> join(v: K1<T, K1<T, A>>): IO<A> = monad.join(v)
    private val monad = object: IOMonad {}
    /** Builds an [IO] with monad comprehension syntax. */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): IO<A> = narrow(monad.fx(block))

    // ApplicativeOps
    /** Lifts a unary function to operate on [IO] values. */
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    /** Lifts a binary function to operate on [IO] values. */
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    /** Lifts a ternary function to operate on [IO] values. */
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: IOApplicativeOps {}

    // MonadOps
    /** Lifts a unary function using monadic bind. */
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    /** Lifts a binary function using monadic bind. */
    fun <A, B, C> liftM2(f: (A, B) -> C) = monadOps.liftM2(f)
    /** Lifts a ternary function using monadic bind. */
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) = monadOps.liftM3(f)
    private val monadOps = object: IOMonadOps {}

    // MonadError
    /** Creates an [IO] that throws [e] when run. */
    fun <A> raiseError(e: Throwable): IO<A> = monadError.raiseError(e)
    private val monadError = object: IOMonadError {}
  }

  // Monad
  /** Maps the result of this effect. */
  infix fun <B> fmap(f: (A) -> B): IO<B> = monad.fmap(f, this)
  /** Runs [f], then this effect, and applies the function to the result. */
  infix fun <B> ap(f: IO<(A) -> B>): IO<B> = monad.ap(f, this)
  /** Sequences this effect with [f]. */
  infix fun <B> bind(f: (A) -> IO<B>): IO<B> = monad.bind(f, this)
  private val monad = object: IOMonad {}

  // MonadError
  /** Recovers from any [Throwable] thrown by this effect with another effect. */
  infix fun handleErrorWith(f: (Throwable) -> IO<A>): IO<A> = monadError.handleErrorWith(f, this)
  /** Recovers from any [Throwable] thrown by this effect with a pure value. */
  infix fun handleError(f: (Throwable) -> A): IO<A> = narrow(monadError.handleError(f, this))
  /** Captures a thrown [Throwable] as `Either.Left`. */
  fun attempt(): IO<Either<Throwable, A>> = narrow(monadError.attempt(this))
  private val monadError = object: IOMonadError {}

  /** Performs the effect and returns its result, rethrowing any exception. */
  fun unsafeRun(): A = run()
}
