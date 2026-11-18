package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * The trivial container holding exactly one [value] with no additional effect.
 *
 * Useful as the base monad for monad transformers, as a
 * stack-safe interpreter target, and for testing generic type class code.
 *
 * Instances: [IdentityFunctor], [IdentityApplicative], [IdentityApplicativeOps],
 * [IdentityMonad], [IdentityMonadOps], [IdentityMonadRec], [IdentityMonadZip],
 * [IdentityFoldable], [IdentityTraversable], [IdentityCopointed], [IdentityComonad],
 * [IdentityDistributive], [IdentityRepresentable].
 *
 * ```kotlin
 * val r = Identity(1) bind { x -> Identity(x + 1) } fmap { it * 10 } // Identity(20)
 * Identity.fx { Identity(1).bind() + Identity(2).bind() } // Identity(3)
 * ```
 */
data class Identity<A>(val value: A): K1<Identity.T, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Identity]. */
    fun <A> narrow(i: K1<T, A>): Identity<A> = i as Identity<A>

    // Monad
    /** Wraps a value into [Identity]. */
    fun <A> pure(v: A): Identity<A> = monad.pure(v)
    /** Flattens a nested [Identity]. */
    fun <A> join(v: K1<T, K1<T, A>>): Identity<A> = monad.join(v)
    private val monad = object: IdentityMonad {}
    /** Builds an [Identity] with monad comprehension syntax. */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Identity<A> = narrow(monad.fx(block))

    // ApplicativeOps1
    /** Lifts a unary function to operate on [Identity] values. */
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    /** Lifts a binary function to operate on [Identity] values. */
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    /** Lifts a ternary function to operate on [Identity] values. */
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: IdentityApplicativeOps {}

    // MonadOps
    /** Lifts a unary function using monadic bind. */
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    /** Lifts a binary function using monadic bind. */
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    /** Lifts a ternary function using monadic bind. */
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)
    private val monadOps = object: IdentityMonadOps {}

    // MonadZip
    /** Splits an [Identity] of a pair into a pair of [Identity] values. */
    infix fun <A, B> munzip(m: K1<T, Pair<A, B>>): Pair<Identity<A>, Identity<B>> = monadZip.munzip(m)
    private val monadZip = object: IdentityMonadZip {}

    // Traversable
    /** Swaps an [Identity] of an effect into an effect of [Identity]. */
    fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> = traversable.sequence(applicative, v)
    private val traversable = object: IdentityTraversable {}
  }

  // Monad
  /** Maps the held value. */
  infix fun <B> fmap(f: (A) -> B): Identity<B> = monad.fmap(f, this)
  /** Applies the function held by [f] to the held value. */
  infix fun <B> ap(f: Identity<(A) -> B>): Identity<B> = monad.ap(f, this)
  /** Applies [f] to the held value. */
  infix fun <B> bind(f: (A) -> Identity<B>): Identity<B> = monad.bind(f, this)
  private val monad = object: IdentityMonad {}

  // Foldable
  /** Folds the single value from the left. */
  fun <B> foldl(initial: B, f: (B, A) -> B): B = foldable.foldl(initial, f, this)
  /** Folds the single value from the right. */
  fun <B> foldr(initial: B, f: (A, B) -> B): B = foldable.foldr(initial, f, this)
  /** Maps the held value into the monoid [type]. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = foldable.foldMap(type, f, this)
  private val foldable = object: IdentityFoldable {}

  // Traversable
  /** Applies the effectful [f] to the held value and wraps the result in [Identity]. */
  fun <G, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse(applicative, f, this)
  private val traversable = object: IdentityTraversable {}

  // MonadZip
  /** Pairs this value with the value of [m]. */
  infix fun <B> mzip(m: K1<T, B>): Identity<Pair<A, B>> = monadZip.mzip(this, m)
  /** Combines this value with the value of [m] using [f]. */
  fun <B, C> mzipWith(m: K1<T, B>, f: (A,  B) -> C): Identity<C> = monadZip.mzipWith(this, m, f)
  private val monadZip = object: IdentityMonadZip {}

  // Comonad
  /** Returns the held value. */
  fun extract(): A = comonad.extract(this)
  /** Wraps this [Identity] in another [Identity]. */
  fun duplicate(): Identity<K1<T, A>> = comonad.duplicate(this)
  /** Applies [f] to this whole [Identity]. */
  fun <B> extend(f: (K1<T, A>) -> B): Identity<B> = comonad.extend(f, this)
  private val comonad = object: IdentityComonad {}
}

