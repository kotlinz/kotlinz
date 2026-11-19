package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A pair of values `(first, second)` encoded as a higher-kinded type.
 *
 * `Tuple2<S, A>` is a functor and comonad in its second component; the first component acts as an
 * environment. When `S` is a [com.github.kotlinz.kotlinz.type.group.Monoid] it is also a writer-like
 * monad that accumulates `first`. Use it when you need a pair that participates in type class code.
 *
 * Instances: [Tuple2Functor], [Tuple2Copointed], [Tuple2Comonad], [Tuple2Bifunctor], [Tuple2Bifoldable] and, for a
 * monoidal first component, [Tuple2Applicative], [Tuple2Monad], [Tuple2ApplicativeOps], [Tuple2MonadOps].
 *
 * ```kotlin
 * val type = StringMonoid::class.java
 * val t = Tuple2(StringMonoid("a"), 3)
 * t.fmap { it + 1 }                                        // Tuple2(StringMonoid("a"), 4)
 * t.bind(type) { x -> Tuple2(StringMonoid("b"), x * 2) }   // Tuple2(StringMonoid("ab"), 6)
 * t.extract()                                              // 3
 * ```
 */
data class Tuple2<A, B>(val first: A, val second: B): K2<Tuple2.T, A, B> {
  /** Higher-kinded witness for [Tuple2]. */
  class T

  companion object {
    /** Downcasts a higher-kinded value to [Tuple2]. */
    fun <A, B> narrow(v: K1<K1<T, A>, B>) = v as Tuple2<A, B>

    // Monad
    /** Wraps [v] with the empty monoid value as first component. */
    fun <S: Monoid<*, S>, A> pure(type: Class<S>, v: A): Tuple2<S, A> = monad(type).pure(v)
    /** Flattens a nested tuple, combining both first components. */
    fun <S: Monoid<*, S>, A> join(type: Class<S>, v: K1<K1<T, S>, K1<K1<T, S>, A>>): Tuple2<S, A> = monad(type).join(v)
    internal fun <S: Monoid<*, S>> monad(type: Class<S>) = object: Tuple2Monad<S> { override val type = type }
    /** Runs a monad comprehension block in the [Tuple2] monad. */
    fun <S: Monoid<*, S>, A> fx(type: Class<S>, block: suspend MonadScope<K1<T, S>>.() -> A): Tuple2<S, A> = narrow(monad(type).fx(block))

    // ApplicativeOps
    /** Lifts a unary function into [Tuple2] applicatively. */
    fun <S: Monoid<*, S>, A, B> liftA(type: Class<S>, f: (A) -> B) = applicativeOps(type).liftA(f)
    /** Lifts a binary function into [Tuple2] applicatively. */
    fun <S: Monoid<*, S>, A, B, C> liftA2(type: Class<S>, f: (A, B) -> C) = applicativeOps(type).liftA2(f)
    /** Lifts a ternary function into [Tuple2] applicatively. */
    fun <S: Monoid<*, S>, A, B, C, D> liftA3(type: Class<S>, f: (A, B, C) -> D) = applicativeOps(type).liftA3(f)
    private fun <S: Monoid<*, S>> applicativeOps(type: Class<S>) = object: Tuple2ApplicativeOps<S> { override val type = type }

    // MonadOps
    /** Lifts a unary function into [Tuple2] monadically. */
    fun <S: Monoid<*, S>, A, B> liftM(type: Class<S>, f: (A) -> B) = monadOps(type).liftM(f)
    /** Lifts a binary function into [Tuple2] monadically. */
    fun <S: Monoid<*, S>, A, B, C> liftM2(type: Class<S>, f: (A, B) -> C) = monadOps(type).liftM2(f)
    /** Lifts a ternary function into [Tuple2] monadically. */
    fun <S: Monoid<*, S>, A, B, C, D> liftM3(type: Class<S>, f: (A, B, C) -> D) = monadOps(type).liftM3(f)
    private fun <S: Monoid<*, S>> monadOps(type: Class<S>) = object: Tuple2MonadOps<S> { override val type = type }
  }

  // Functor
  /** Maps the second component. */
  infix fun <C> fmap(f: (B) -> C): Tuple2<A, C> = comonad.fmap(f, this)

  // Comonad
  /** Returns the second component. */
  fun extract(): B = comonad.extract(this)
  /** Replaces the second component with the whole tuple. */
  fun duplicate(): Tuple2<A, K1<K1<T, A>, B>> = comonad.duplicate(this)
  /** Applies [f] to the whole tuple, keeping the first component. */
  fun <C> extend(f: (K1<K1<T, A>, B>) -> C): Tuple2<A, C> = comonad.extend(f, this)
  private val comonad = object: Tuple2Comonad<A> {}

  // Bifunctor (first / second are omitted as members because they clash with the properties)
  /** Maps both components. */
  fun <C, D> bimap(f: (A) -> C, g: (B) -> D): Tuple2<C, D> = bifunctor.bimap(f, g, this)
  private val bifunctor = object: Tuple2Bifunctor {}

  // Bifoldable
  /** Left-folds [first] with [f], then [second] with [g]. */
  fun <C> bifoldLeft(initial: C, f: (C, A) -> C, g: (C, B) -> C): C = bifoldable.bifoldLeft(initial, f, g, this)
  /** Right-folds [first] with [f] and [second] with [g]. */
  fun <C> bifoldRight(initial: C, f: (A, C) -> C, g: (B, C) -> C): C = bifoldable.bifoldRight(initial, f, g, this)
  /** Maps both components into the monoid [M] and combines them. */
  fun <M: Monoid<*, M>> bifoldMap(type: Class<M>, f: (A) -> M, g: (B) -> M): M = bifoldable.bifoldMap(type, f, g, this)
  private val bifoldable = object: Tuple2Bifoldable {}

  /** Converts to a Kotlin [Pair]. */
  fun toPair(): Pair<A, B> = Pair(first, second)
}

// Monad
/** Applies the function in [f], combining first components. */
fun <S: Monoid<*, S>, A, B> Tuple2<S, A>.ap(type: Class<S>, f: Tuple2<S, (A) -> B>): Tuple2<S, B> = Tuple2.monad(type).ap(f, this)
/** Sequences [f], combining first components. */
fun <S: Monoid<*, S>, A, B> Tuple2<S, A>.bind(type: Class<S>, f: (A) -> Tuple2<S, B>): Tuple2<S, B> = Tuple2.monad(type).bind(f, this)

/** Converts a Kotlin [Pair] to [Tuple2]. */
fun <A, B> Pair<A, B>.toTuple2(): Tuple2<A, B> = Tuple2(first, second)
