package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.group.Semigroup

/**
 * An inclusive-or: a [Left] value, a [Right] value, or [Both] at once.
 *
 * Like [com.github.kotlinz.kotlinz.data.either.Either] but able to carry warnings next to a
 * successful result. Left values are accumulated with a [Semigroup], and computation stops
 * only on a pure [Left].
 *
 * Instances: [IorFunctor], [IorBifunctor], [IorBifoldable], [IorBitraversable], and — given a Semigroup class for the left
 * type — [IorApplicative], [IorApplicativeOps], [IorMonad], [IorMonadOps].
 *
 * ```kotlin
 * val type = StringMonoid::class.java
 * val f = { x: Int -> Ior.both<StringMonoid, Int>(StringMonoid("f"), x + 1) }
 * Ior.both<StringMonoid, Int>(StringMonoid("a"), 1).bind(type, f) // Both(StringMonoid("af"), 2)
 * ```
 */
sealed class Ior<A, B>: K2<Ior.T, A, B> {
  class T

  /** Only a left value. */
  data class Left<A, B>(val value: A): Ior<A, B>()
  /** Only a right value. */
  data class Right<A, B>(val value: B): Ior<A, B>()
  /** Both a left and a right value. */
  data class Both<A, B>(val left: A, val right: B): Ior<A, B>()

  companion object {
    /** Downcasts a higher-kinded representation back to [Ior]. */
    fun <A, B> narrow(v: K1<K1<T, A>, B>) = v as Ior<A, B>

    /** Creates a [Left]. */
    fun <A, B> left(v: A): Ior<A, B> = Left(v)
    /** Creates a [Right]. */
    fun <A, B> right(v: B): Ior<A, B> = Right(v)
    /** Creates a [Both]. */
    fun <A, B> both(a: A, b: B): Ior<A, B> = Both(a, b)

    // Bitraversable
    /** Turns an [Ior] of effects on both sides into an effect of an [Ior]. */
    fun <G, A, B> bisequence(applicative: Applicative<G>, v: K2<T, K1<G, A>, K1<G, B>>): K1<G, K2<T, A, B>> = bitraversable.bisequence(applicative, v)
    private val bitraversable = object: IorBitraversable {}

    // Monad
    /** Wraps a value in [Right]. */
    fun <A: Semigroup<*, A>, B> pure(type: Class<A>, v: B): Ior<A, B> = monad(type).pure(v)
    /** Flattens a nested [Ior], accumulating left values. */
    fun <A: Semigroup<*, A>, B> join(type: Class<A>, v: K1<K1<T, A>, K1<K1<T, A>, B>>): Ior<A, B> = monad(type).join(v)
    internal fun <A: Semigroup<*, A>> monad(type: Class<A>) = object: IorMonad<A> { override val type = type }
    /** Builds an [Ior] with monad comprehension syntax, accumulating left values. */
    fun <A: Semigroup<*, A>, B> fx(type: Class<A>, block: suspend MonadScope<K1<T, A>>.() -> B): Ior<A, B> = narrow(monad(type).fx(block))

    // ApplicativeOps
    /** Lifts a unary function to operate on [Ior] values. */
    fun <S: Semigroup<*, S>, A, B> liftA(type: Class<S>, f: (A) -> B) = applicativeOps(type).liftA(f)
    /** Lifts a binary function to operate on [Ior] values. */
    fun <S: Semigroup<*, S>, A, B, C> liftA2(type: Class<S>, f: (A, B) -> C) = applicativeOps(type).liftA2(f)
    /** Lifts a ternary function to operate on [Ior] values. */
    fun <S: Semigroup<*, S>, A, B, C, D> liftA3(type: Class<S>, f: (A, B, C) -> D) = applicativeOps(type).liftA3(f)
    private fun <S: Semigroup<*, S>> applicativeOps(type: Class<S>) = object: IorApplicativeOps<S> { override val type = type }

    // MonadOps
    /** Lifts a unary function using monadic bind. */
    fun <S: Semigroup<*, S>, A, B> liftM(type: Class<S>, f: (A) -> B) = monadOps(type).liftM(f)
    /** Lifts a binary function using monadic bind. */
    fun <S: Semigroup<*, S>, A, B, C> liftM2(type: Class<S>, f: (A, B) -> C) = monadOps(type).liftM2(f)
    /** Lifts a ternary function using monadic bind. */
    fun <S: Semigroup<*, S>, A, B, C, D> liftM3(type: Class<S>, f: (A, B, C) -> D) = monadOps(type).liftM3(f)
    private fun <S: Semigroup<*, S>> monadOps(type: Class<S>) = object: IorMonadOps<S> { override val type = type }
  }

  // Functor
  /** Maps the right value, if any. */
  infix fun <C> fmap(f: (B) -> C): Ior<A, C> = functor.fmap(f, this)
  private val functor = object: IorFunctor<A> {}

  // Bifunctor
  /** Maps the left value with [f] and the right value with [g]. */
  fun <C, D> bimap(f: (A) -> C, g: (B) -> D): Ior<C, D> = bifunctor.bimap(f, g, this)
  /** Maps the left value, if any. */
  infix fun <C> first(f: (A) -> C): Ior<C, B> = bifunctor.first(f, this)
  /** Maps the right value, if any. */
  infix fun <D> second(g: (B) -> D): Ior<A, D> = bifunctor.second(g, this)
  private val bifunctor = object: IorBifunctor {}

  // Bifoldable
  /** Left-folds the left value with [f] and the right value with [g], left first. */
  fun <C> bifoldLeft(initial: C, f: (C, A) -> C, g: (C, B) -> C): C = bifoldable.bifoldLeft(initial, f, g, this)
  /** Right-folds the left value with [f] and the right value with [g], left first. */
  fun <C> bifoldRight(initial: C, f: (A, C) -> C, g: (B, C) -> C): C = bifoldable.bifoldRight(initial, f, g, this)
  /** Maps the left value with [f] and the right value with [g] into the monoid [M] and combines them. */
  fun <M: Monoid<*, M>> bifoldMap(type: Class<M>, f: (A) -> M, g: (B) -> M): M = bifoldable.bifoldMap(type, f, g, this)
  private val bifoldable = object: IorBifoldable {}

  // Bitraversable
  /** Applies the effectful [f] to the left value and [g] to the right value, left first, and collects the result inside [G]. */
  fun <G, C, D> bitraverse(applicative: Applicative<G>, f: (A) -> K1<G, C>, g: (B) -> K1<G, D>): K1<G, K2<T, C, D>> = bitraversable.bitraverse(applicative, f, g, this)
  private val bitraversable = object: IorBitraversable {}
}

// Monad
/** Applies the function in [f] to the right value, accumulating left values with [type]. */
fun <A: Semigroup<*, A>, B, C> Ior<A, B>.ap(type: Class<A>, f: Ior<A, (B) -> C>): Ior<A, C> = Ior.monad(type).ap(f, this)
/** Sequences with [f], accumulating left values with [type]. */
fun <A: Semigroup<*, A>, B, C> Ior<A, B>.bind(type: Class<A>, f: (B) -> Ior<A, C>): Ior<A, C> = Ior.monad(type).bind(f, this)
