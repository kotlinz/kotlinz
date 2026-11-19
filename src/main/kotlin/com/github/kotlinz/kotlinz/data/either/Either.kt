package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A value that is either a [Left] (conventionally an error) or a [Right] (a success).
 *
 * Use it for computations that can fail with a typed error and should short-circuit on the first failure.
 * Right-biased: [fmap], [ap] and [bind] act on the [Right] value and pass a [Left] through unchanged.
 *
 * Instances: Functor, Applicative, Selective, Monad, MonadError (error type `L`), Foldable, Traversable and Bifunctor.
 *
 * ```kotlin
 * val parse = { s: String -> s.toIntOrNull()?.let { Either.Right<String, Int>(it) } ?: Either.Left("not a number: $s") }
 * val result = parse("21") bind { n -> Either.pure<String, Int>(n * 2) } // Right(42)
 * val recovered = parse("x") handleError { 0 }                           // Right(0)
 * ```
 */
sealed class Either<L, R>: K2<Either.T, L, R> {
  class T

  /** The left (failure) case holding [value]. */
  class Left<L, R>(val value: L): Either<L, R>()
  /** The right (success) case holding [value]. */
  class Right<L, R>(val value: R): Either<L, R>()

  companion object {
    /** Downcasts a higher-kinded representation back to [Either]. */
    fun <L, R> narrow(v: K1<K1<T, L>, R>) = v as Either<L, R>

    // Monad
    /** Wraps [v] in [Right]. */
    fun <L, S> pure(v: S): Either<L, S> = monad<L>().pure(v)
    /** Flattens a nested [Either]. */
    fun <L, S> join(v: K1<K1<T, L>, K1<K1<T, L>, S>>): Either<L, S> = monad<L>().join(v)
    private fun <L> monad() = object: EitherMonad<L> {}
    /** Runs a monad comprehension [block] in the [Either] monad. */
    fun <L, R> fx(block: suspend MonadScope<K1<T, L>>.() -> R): Either<L, R> = narrow(monad<L>().fx(block))

    // ApplicativeOps
    /** Lifts a unary function to operate on [Either] values via Applicative. */
    fun <S, A, B> liftA(f: (A) -> B) = applicativeOps<S>().liftA(f)
    /** Lifts a binary function to operate on [Either] values via Applicative. */
    fun <S, A, B, C> liftA2(f: (A, B) -> C) = applicativeOps<S>().liftA2(f)
    /** Lifts a ternary function to operate on [Either] values via Applicative. */
    fun <S, A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps<S>().liftA3(f)
    private fun <S> applicativeOps() = object: EitherApplicativeOps<S> {}

    // MonadOps
    /** Lifts a unary function to operate on [Either] values via Monad. */
    fun <S, A, B> liftM(f: (A) -> B) = monadOps<S>().liftM(f)
    /** Lifts a binary function to operate on [Either] values via Monad. */
    fun <S, A, B, C> liftM2(f: (A,  B) -> C) =  monadOps<S>().liftM2(f)
    /** Lifts a ternary function to operate on [Either] values via Monad. */
    fun <S, A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps<S>().liftM3(f)
    private fun <S> monadOps() = object: EitherMonadOps<S> {}

    // Traversable
    /** Turns an [Either] of an effect into an effect of an [Either]. */
    fun <L, G, R> sequence(applicative: Applicative<G>, v: K1<K1<T, L>, K1<G, R>>): K1<G, K1<K1<T, L>, R>> = traversable<L>().sequence(applicative, v)
    private fun <L> traversable() = object: EitherTraversable<L> {}

    // MonadError
    /** Creates a [Left] holding the error [e]. */
    fun <L, R> raiseError(e: L): Either<L, R> = monadError<L>().raiseError(e)
    private fun <L> monadError() = object: EitherMonadError<L> {}

    // Selective
    /** Runs [fn] only when [fab] is `Right(Left)`, applying it to the inner left value. */
    fun <L, A, B> select(fab: K1<K1<T, L>, Either<A, B>>, fn: K1<K1<T, L>, (A) -> B>): Either<L, B> = selective<L>().select(fab, fn)
    /** Chooses [t] or [e] depending on [cond]; only the chosen branch is inspected. */
    fun <L, A> ifS(cond: K1<K1<T, L>, Boolean>, t: K1<K1<T, L>, A>, e: K1<K1<T, L>, A>): Either<L, A> = narrow(selective<L>().ifS(cond, t, e))
    private fun <L> selective() = object: EitherSelective<L> {}
  }

  // Monad
  /** Maps the [Right] value with [f]. */
  infix fun <S> fmap(f: (R) -> S): Either<L, S> = monad.fmap(f, this)
  /** Applies the function inside [f] to the [Right] value; the first [Left] wins. */
  infix fun <S> ap(f: Either<L, (R) -> S>): Either<L, S> = monad.ap(f, this)
  /** Chains a computation on the [Right] value, short-circuiting on [Left]. */
  infix fun <S> bind(f: (R) -> Either<L, S>): Either<L, S> = monad.bind(f, this)
  private val monad = object: EitherMonad<L> {}

  // MonadError
  /** Recovers from a [Left] by running [f] on the error. */
  infix fun handleErrorWith(f: (L) -> Either<L, R>): Either<L, R> = monadError.handleErrorWith(f, this)
  /** Recovers from a [Left] by mapping the error to a success value. */
  infix fun handleError(f: (L) -> R): Either<L, R> = narrow(monadError.handleError(f, this))
  private val monadError = object: EitherMonadError<L> {}

  // Foldable
  /** Left-folds over the [Right] value (zero or one element). */
  fun <B> foldl(initial: B, f: (B, R) -> B): B = foldable.foldl(initial, f, this)
  /** Right-folds over the [Right] value (zero or one element). */
  fun <B> foldr(initial: B, f: (R, B) -> B): B = foldable.foldr(initial, f, this)
  /** Maps the [Right] value into the monoid [M], or returns its identity for [Left]. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (R) -> M): M = foldable.foldMap(type, f, this)
  private val foldable = object: EitherFoldable<L> {}

  // Traversable
  /** Applies the effectful [f] to the [Right] value and collects the result inside [G]. */
  fun <G, B> traverse(applicative: Applicative<G>, f: (R) -> K1<G, B>): K1<G, K1<K1<T, L>, B>> = traversable.traverse(applicative, f, this)
  private val traversable = object: EitherTraversable<L> {}

  // Bifunctor
  /** Maps the [Left] value with [f] and the [Right] value with [g]. */
  fun <M, S> bimap(f: (L) -> M, g: (R) -> S): Either<M, S> = bifunctor.bimap(f, g, this)
  /** Maps the [Left] value with [f]. */
  infix fun <M> first(f: (L) -> M): Either<M, R> = bifunctor.first(f, this)
  /** Maps the [Right] value with [g]. */
  infix fun <S> second(g: (R) -> S): Either<L, S> = bifunctor.second(g, this)
  private val bifunctor = object: EitherBifunctor {}

  override fun equals(other: Any?): Boolean {
    return when {
      this is Left && other is Left<*, *> -> this.value == other.value
      this is Right && other is Right<*, *> -> this.value == other.value
      else -> false
    }
  }
}