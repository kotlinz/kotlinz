package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

/**
 * A lazily evaluated, memoized value.
 *
 * Use it to defer an expensive computation until [value] is first read; the result is cached afterwards.
 * [now] wraps an already-computed value and [later] defers a thunk (both memoize; there is no re-evaluating variant).
 * Note that deep chains of [bind] / [fmap] are evaluated recursively; use [EvalMonadRec.tailRecM] for loops.
 *
 * Instances: Functor, Applicative, Monad, MonadRec, Copointed and Comonad.
 *
 * ```kotlin
 * val eval = Eval.later { expensive() } fmap { it * 10 } // nothing computed yet
 * eval.value                                            // computes once, then cached
 * Eval.now(42).extend { Eval.narrow(it).value + 1 }.extract() // 43
 * ```
 */
class Eval<A>(private val thunk: () -> A): K1<Eval.T, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Eval]. */
    fun <A> narrow(v: K1<T, A>): Eval<A> = v as Eval<A>

    /** Wraps an already evaluated value [v]. */
    fun <A> now(v: A): Eval<A> = Eval { v }
    /** Defers [f] until the value is first requested, then memoizes the result. */
    fun <A> later(f: () -> A): Eval<A> = Eval(f)

    // Monad
    /** Wraps [v] in [Eval]; same as [now]. */
    fun <A> pure(v: A): Eval<A> = monad.pure(v)
    /** Flattens a nested [Eval] lazily. */
    fun <A> join(v: K1<T, K1<T, A>>): Eval<A> = monad.join(v)
    private val monad = object: EvalMonad {}
    /** Runs a monad comprehension [block] in the [Eval] monad. */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Eval<A> = narrow(monad.fx(block))

    // ApplicativeOps
    /** Lifts a unary function to operate on [Eval] values via Applicative. */
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    /** Lifts a binary function to operate on [Eval] values via Applicative. */
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    /** Lifts a ternary function to operate on [Eval] values via Applicative. */
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: EvalApplicativeOps {}

    // MonadOps
    /** Lifts a unary function to operate on [Eval] values via Monad. */
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    /** Lifts a binary function to operate on [Eval] values via Monad. */
    fun <A, B, C> liftM2(f: (A, B) -> C) = monadOps.liftM2(f)
    /** Lifts a ternary function to operate on [Eval] values via Monad. */
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) = monadOps.liftM3(f)
    private val monadOps = object: EvalMonadOps {}
  }

  /** The computed value; evaluated on first access and memoized. */
  val value: A by lazy { thunk() }

  // Monad
  /** Lazily maps the value with [f]. */
  infix fun <B> fmap(f: (A) -> B): Eval<B> = monad.fmap(f, this)
  /** Lazily applies the function inside [f] to the value. */
  infix fun <B> ap(f: Eval<(A) -> B>): Eval<B> = monad.ap(f, this)
  /** Lazily chains a computation on the value. */
  infix fun <B> bind(f: (A) -> Eval<B>): Eval<B> = monad.bind(f, this)
  private val monad = object: EvalMonad {}

  // Comonad
  /** Forces and returns the value. */
  fun extract(): A = comonad.extract(this)
  /** Wraps this [Eval] in another [Eval]. */
  fun duplicate(): Eval<K1<T, A>> = comonad.duplicate(this)
  /** Lazily computes a new value from this whole [Eval] with [f]. */
  fun <B> extend(f: (K1<T, A>) -> B): Eval<B> = comonad.extend(f, this)
  private val comonad = object: EvalComonad {}

  /** Compares by forcing both values. */
  override fun equals(other: Any?): Boolean = other is Eval<*> && value == other.value
  override fun hashCode(): Int = value?.hashCode() ?: 0
}
