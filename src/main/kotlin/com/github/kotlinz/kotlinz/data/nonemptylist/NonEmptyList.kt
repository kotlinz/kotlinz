package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A list that is guaranteed to contain at least one element.
 *
 * Use it when emptiness is not a valid state, e.g. a collection of validation errors
 * or a non-empty set of choices, so that [head] and [extract] are always safe.
 * Has instances for Functor, Applicative, Monad, Foldable, Traversable, Copointed, Comonad, Semialign and Zip.
 *
 * ```kotlin
 * val xs = nonEmptyListOf(1, 2, 3)
 * xs bind { nonEmptyListOf(it, it * 10) } // nonEmptyListOf(1, 10, 2, 20, 3, 30)
 * xs.extend { NonEmptyList.narrow(it).toList().sum() } // nonEmptyListOf(6, 5, 3)
 * NonEmptyList.fromList(emptyList<Int>()) // null
 * ```
 */
data class NonEmptyList<A>(val head: A, val tail: List<A>): K1<NonEmptyList.T, A> {
  /** Higher-kinded witness tag for [NonEmptyList]. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [NonEmptyList]. */
    fun <A> narrow(v: K1<T, A>): NonEmptyList<A> = v as NonEmptyList<A>

    // Monad
    /** Creates a singleton [NonEmptyList]. */
    fun <A> pure(v: A): NonEmptyList<A> = monad.pure(v)
    /** Flattens a nested [NonEmptyList] by concatenation. */
    fun <A> join(v: K1<T, K1<T, A>>): NonEmptyList<A> = monad.join(v)
    private val monad = object: NonEmptyListMonad {}
    /** Runs a monad comprehension; the block is replayed for every element (non-deterministic choice). */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): NonEmptyList<A> = narrow(monad.fxReplay(block))

    // ApplicativeOps
    /** Lifts a unary function into [NonEmptyList] using the Applicative instance. */
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    /** Lifts a binary function into [NonEmptyList] using the Applicative instance. */
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    /** Lifts a ternary function into [NonEmptyList] using the Applicative instance. */
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: NonEmptyListApplicativeOps {}

    // MonadOps
    /** Lifts a unary function into [NonEmptyList] using the Monad instance. */
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    /** Lifts a binary function into [NonEmptyList] using the Monad instance. */
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    /** Lifts a ternary function into [NonEmptyList] using the Monad instance. */
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)
    private val monadOps = object: NonEmptyListMonadOps {}

    /** Converts a [List] to a [NonEmptyList], returning `null` when the list is empty. */
    fun <A> fromList(list: List<A>): NonEmptyList<A>? = list.firstOrNull()?.let { NonEmptyList(it, list.drop(1)) }

    // Traversable
    /** Turns a [NonEmptyList] of effects into an effect of a [NonEmptyList]. */
    fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> = traversable.sequence(applicative, v)
    private val traversable = object: NonEmptyListTraversable {}
  }

  // Monad
  /** Applies [f] to every element. */
  infix fun <B> fmap(f: (A) -> B): NonEmptyList<B> = monad.fmap(f, this)
  /** Applies every function in [f] to every element (cartesian product). */
  infix fun <B> ap(f: K1<T, (A) -> B>): NonEmptyList<B> = monad.ap(f, this)
  /** Maps every element to a [NonEmptyList] and concatenates the results. */
  infix fun <B> bind(f: (A) -> K1<T, B>): NonEmptyList<B> = monad.bind(f, this)
  private val monad = object: NonEmptyListMonad {}

  // Foldable
  /** Left-associative fold over all elements. */
  fun <B> foldl(initial: B, f: (B, A) -> B): B = foldable.foldl(initial, f, this)
  /** Right-associative fold over all elements. */
  fun <B> foldr(initial: B, f: (A, B) -> B): B = foldable.foldr(initial, f, this)
  /** Maps every element into the Monoid [type] and combines the results. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = foldable.foldMap(type, f, this)
  private val foldable = object: NonEmptyListFoldable {}

  // Traversable
  /** Maps every element to an effect and collects the results inside the effect. */
  fun <G, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse(applicative, f, this)
  private val traversable = object: NonEmptyListTraversable {}

  // Comonad
  /** Returns the [head]. */
  fun extract(): A = comonad.extract(this)
  /** Returns the non-empty suffixes of this list (the list itself first). */
  fun duplicate(): NonEmptyList<K1<T, A>> = comonad.duplicate(this)
  /** Applies [f] to every non-empty suffix. */
  fun <B> extend(f: (K1<T, A>) -> B): NonEmptyList<B> = comonad.extend(f, this)
  private val comonad = object: NonEmptyListComonad {}

  // Semialign / Zip
  /** Pairs this list with [m] by index, keeping the leftover elements of the longer one. */
  infix fun <B> align(m: K1<T, B>): NonEmptyList<Ior<A, B>> = zip.align(this, m)
  /** Combines this list with [m] by index using [f], keeping the leftover elements of the longer one. */
  fun <B, C> alignWith(m: K1<T, B>, f: (Ior<A, B>) -> C): NonEmptyList<C> = zip.alignWith(f, this, m)
  /** Pairs this list with [m] by index, truncating to the shorter one. */
  infix fun <B> zip(m: K1<T, B>): NonEmptyList<Pair<A, B>> = zip.zip(this, m)
  /** Combines this list with [m] by index using [f], truncating to the shorter one. */
  fun <B, C> zipWith(m: K1<T, B>, f: (A, B) -> C): NonEmptyList<C> = zip.zipWith(f, this, m)
  private val zip = object: NonEmptyListZip {}

  /** Converts to a plain [List]. */
  fun toList(): List<A> = listOf(head) + tail
  /** Concatenates two non-empty lists. */
  operator fun plus(other: NonEmptyList<A>): NonEmptyList<A> = NonEmptyList(head, tail + other.toList())
}

/** Creates a [NonEmptyList] from a head and optional further elements. */
fun <A> nonEmptyListOf(head: A, vararg tail: A): NonEmptyList<A> = NonEmptyList(head, tail.toList())
