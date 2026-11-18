package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A computation that either succeeded with a value ([Success]) or failed with an exception ([Failure]).
 *
 * Use it to turn exception-throwing code into values that compose. Has Functor, Applicative, Monad,
 * MonadError (with [Throwable]), Foldable and Traversable instances. [fmap] and [bind] catch exceptions
 * thrown by the given function and turn them into [Failure]; only [Exception]s are caught, not [Error]s.
 *
 * ```kotlin
 * Try.of { "42".toInt() }                                     // Success(42)
 * Try.pure(1) fmap { it / 0 }                                 // Failure(ArithmeticException)
 * Try.raiseError<Int>(IllegalStateException()) handleError { 0 } // Success(0)
 * ```
 */
sealed class Try<A>: K1<Try.T, A> {

  class T

  /** A successful result holding [value]. */
  class Success<A>(val value: A): Try<A>()
  /** A failed result holding the thrown [error]. */
  class Failure<A>(val error: Throwable): Try<A>()

  companion object {
    /** Downcasts a higher-kinded value to its concrete type. */
    fun <A> narrow(v: K1<T, A>): Try<A> = v as Try<A>

    /** Runs `f`, capturing a thrown [Exception] as [Failure]. */
    fun <A> of(f: () -> A): Try<A> {
      return try {
        Success(f())
      } catch (e: Exception) {
        Failure(e)
      }
    }

    // Monad
    /** Wraps `v` in [Success]. */
    fun <A> pure(v: A): Try<A> = monad.pure(v)
    /** Flattens a nested Try. */
    fun <A> join(v: K1<T, K1<T, A>>): Try<A> = monad.join(v)
    private val monad = object: TryMonad {}
    /** Builds a Try using monad comprehension syntax. */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Try<A> = narrow(monad.fx(block))

    // ApplicativeOps
    /** Lifts a unary function to operate on Try values (applicative style). */
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    /** Lifts a binary function to operate on Try values (applicative style). */
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    /** Lifts a ternary function to operate on Try values (applicative style). */
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: TryApplicativeOps {}

    // MonadOps
    /** Lifts a unary function to operate on Try values (monadic style). */
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    /** Lifts a binary function to operate on Try values (monadic style). */
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    /** Lifts a ternary function to operate on Try values (monadic style). */
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)
    private val monadOps = object: TryMonadOps {}

    // Traversable
    /** Turns a Try of an applicative value inside out. */
    fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> = traversable.sequence(applicative, v)
    private val traversable = object: TryTraversable {}

    // MonadError
    /** Creates a [Failure] from `e`. */
    fun <A> raiseError(e: Throwable): Try<A> = monadError.raiseError(e)
    private val monadError = object: TryMonadError {}
  }

  // Monad
  /** Maps the success value; an exception thrown by `f` becomes [Failure]. */
  infix fun <B> fmap(f: (A) -> B): Try<B> = monad.fmap(f, this)
  /** Applies a wrapped function to the success value. */
  infix fun <B> ap(f: K1<T, (A) -> B>): Try<B> = monad.ap(f, this)
  /** Chains a Try-returning function; an exception thrown by `f` becomes [Failure]. */
  infix fun <B> bind(f: (A) -> K1<T, B>): Try<B> = monad.bind(f, this)
  private val monad = object: TryMonad {}

  // MonadError
  /** Recovers from [Failure] with another Try. */
  /** Recovers from [Failure] with a plain value. */
  infix fun handleErrorWith(f: (Throwable) -> Try<A>): Try<A> = monadError.handleErrorWith(f, this)
  infix fun handleError(f: (Throwable) -> A): Try<A> = narrow(monadError.handleError(f, this))
  /** Exposes the outcome as an [Either] inside a [Success]. */
  fun attempt(): Try<Either<Throwable, A>> = narrow(monadError.attempt(this))
  private val monadError = object: TryMonadError {}

  // Foldable
  /** Left fold over the success value (zero or one element). */
  fun <B> foldl(initial: B, f: (B, A) -> B): B = foldable.foldl(initial, f, this)
  /** Right fold over the success value (zero or one element). */
  fun <B> foldr(initial: B, f: (A, B) -> B): B = foldable.foldr(initial, f, this)
  /** Maps the success value to a monoid, or returns `mzero` on failure. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = foldable.foldMap(type, f, this)
  private val foldable = object: TryFoldable {}

  // Traversable
  /** Applies an effectful function to the success value and collects the result. */
  fun <G, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse(applicative, f, this)
  private val traversable = object: TryTraversable {}

  /** Returns the success value, or `v` on failure. */
  fun getOrElse(v: A): A {
    return when (this) {
      is Success -> this.value
      is Failure -> v
    }
  }

  override fun equals(other: Any?): Boolean {
    return when {
      this is Success && other is Try.Success<*> -> this.value == other.value
      this is Failure && other is Try.Failure<*> -> this.error == other.error
      else -> false
    }
  }
}
