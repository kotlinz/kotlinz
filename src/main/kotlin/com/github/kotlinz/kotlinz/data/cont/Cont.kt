package com.github.kotlinz.kotlinz.data.cont

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2

/**
 * The continuation monad: a computation that, given a continuation `(A) -> R`, produces the final result R.
 *
 * Use it for early exit ([callCC]), backtracking-style control flow, or CPS-transforming code. Has
 * instances for Functor, Applicative, ApplicativeOps, Monad, MonadOps and MonadCont. Not stack-safe: deeply nested
 * continuations may overflow the stack.
 *
 * ```kotlin
 * val c = callCC<Int, Int, Int> { exit -> exit(10) bind { Cont.pure<Int, Int>(it + 100) } }
 * c.eval() // 10
 * ```
 */
class Cont<R, A>(val run: ((A) -> R) -> R): K2<Cont.T, R, A> {
  /** Type tag used for higher-kinded encoding. */
  class T

  companion object {
    /** Downcasts the higher-kinded representation back to [Cont]. */
    fun <R, A> narrow(v: K1<K1<T, R>, A>) = v as Cont<R, A>

    // Monad
    /** Wraps a pure value. */
    fun <R, A> pure(v: A): Cont<R, A> = monad<R>().pure(v)
    /** Flattens a nested Cont. */
    fun <R, A> join(v: K1<K1<T, R>, K1<K1<T, R>, A>>): Cont<R, A> = monad<R>().join(v)
    private fun <R> monad() = object: ContMonad<R> {}

    // ApplicativeOps
    /** Lifts a unary function into Cont. */
    fun <R, A, B> liftA(f: (A) -> B) = applicativeOps<R>().liftA(f)
    /** Lifts a binary function into Cont. */
    fun <R, A, B, C> liftA2(f: (A, B) -> C) = applicativeOps<R>().liftA2(f)
    /** Lifts a ternary function into Cont. */
    fun <R, A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps<R>().liftA3(f)
    private fun <R> applicativeOps() = object: ContApplicativeOps<R> {}

    // MonadOps
    /** Lifts a unary function into Cont using bind. */
    fun <R, A, B> liftM(f: (A) -> B) = monadOps<R>().liftM(f)
    /** Lifts a binary function into Cont using bind. */
    fun <R, A, B, C> liftM2(f: (A,  B) -> C) =  monadOps<R>().liftM2(f)
    /** Lifts a ternary function into Cont using bind. */
    fun <R, A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps<R>().liftM3(f)
    private fun <R> monadOps() = object: ContMonadOps<R> {}
  }

  // Monad
  /** Maps over the result. */
  infix fun <B> fmap(f: (A) -> B): Cont<R, B> = monad.fmap(f, this)
  /** Applies a wrapped function to the result. */
  infix fun <B> ap(f: Cont<R, (A) -> B>): Cont<R, B> = monad.ap(f, this)
  /** Sequences a dependent computation. */
  infix fun <B> bind(f: (A) -> Cont<R, B>): Cont<R, B> = monad.bind(f, this)
  private val monad = object: ContMonad<R> {}
}

/** Runs the computation with the identity continuation. */
fun <A> Cont<A, A>.eval(): A = run { it }

/** Call with current continuation: passes [f] an escape function that aborts the rest of the computation with its argument. */
fun <R, A, B> callCC(f: ((A) -> Cont<R, B>) -> Cont<R, A>): Cont<R, A> {
  return Cont { k -> f { a -> Cont<R, B> { k(a) } }.run(k) }
}
