package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A catenable sequence with O(1) concatenation, prepend and append.
 *
 * Elements are kept in a tree of appended segments that is only flattened when iterated, which
 * makes `Chain` a good fit for accumulating output (logs, writer monoids) piece by piece.
 * Iteration, [size] and [uncons] never recurse, so very deep chains are stack safe.
 *
 * Instances: Functor, Applicative, Monad, Foldable, Traversable, Semialign, Align, Zip, Filterable,
 * Witherable, and [Monoid]
 * (concatenation, with the public no-arg constructor creating the empty chain).
 *
 * ```kotlin
 * val chain = (Chain.one(1) + chainOf(2, 3)).prepend(0).append(4) // Chain(0, 1, 2, 3, 4)
 * chain bind { chainOf(it, it) }                                   // Chain(0, 0, 1, 1, ...)
 * Chain.fx { chainOf(1, 2).bind() * 10 }                           // Chain(10, 20)
 * ```
 */
class Chain<A> internal constructor(internal val node: ChainNode<A>): K1<Chain.T, A>, Monoid<A, Chain<A>>, Iterable<A> {
  /** Creates the empty chain; required by reflective lookups such as `Monoid.mzero(type)`. */
  constructor(): this(ChainNode.Empty())

  /** Witness type used to encode `Chain` as a higher-kinded `K1<Chain.T, A>`. */
  class T

  companion object {
    /** Downcasts a higher-kinded `K1<Chain.T, A>` back to [Chain]. */
    fun <A> narrow(v: K1<T, A>): Chain<A> = v as Chain<A>

    /** The empty chain. */
    fun <A> empty(): Chain<A> = Chain()
    /** A chain holding the single [value]. */
    fun <A> one(value: A): Chain<A> = Chain(ChainNode.Single(value))
    /** A chain wrapping [values] without copying them; [values] must not be mutated afterwards. */
    fun <A> fromList(values: List<A>): Chain<A> = if (values.isEmpty()) empty() else Chain(ChainNode.Wrap(values))
    /** A first-class concatenation monoid for chains. */
    fun <A> monoid(): MonoidOf<Chain<A>> = MonoidOf(empty()) { a, b -> a + b }

    // Monad
    /** Creates a singleton chain. */
    fun <A> pure(v: A): Chain<A> = monad.pure(v)
    /** Flattens a chain of chains. */
    fun <A> join(v: K1<T, K1<T, A>>): Chain<A> = monad.join(v)
    /** Runs a monad comprehension; the block is replayed for every bound element (non-deterministic choice). */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Chain<A> = narrow(monad.fxReplay(block))

    // Traversable
    /** Turns a chain of `G` effects into a `G` effect of a chain. */
    fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> = traversable.sequence(applicative, v)

    // Align
    /** Returns the empty chain, the identity of [align]. */
    fun <A> nil(): Chain<A> = zip.nil()

    private val monad = object: ChainMonad {}
    private val traversable = object: ChainTraversable {}
    private val witherable = object: ChainWitherable {}
    private val zip = object: ChainZip, ChainAlign {}
  }

  /** The number of elements, in O(1). */
  val size: Int get() = node.size
  /** Returns true when the chain has no elements. */
  fun isEmpty(): Boolean = size == 0
  /** Concatenates [other] after this chain in O(1). */
  operator fun plus(other: Chain<A>): Chain<A> = when {
    isEmpty() -> other
    other.isEmpty() -> this
    else -> Chain(ChainNode.Concat(node, other.node))
  }
  /** Adds [value] in front of this chain in O(1). */
  infix fun prepend(value: A): Chain<A> = one(value) + this
  /** Adds [value] at the end of this chain in O(1). */
  infix fun append(value: A): Chain<A> = this + one(value)
  /** Returns the first element and the rest, or [Maybe.None] when empty. */
  fun uncons(): Maybe<Pair<A, Chain<A>>> = node.uncons()
  /** Iterates the elements in order without recursion. */
  override fun iterator(): Iterator<A> = ChainIterator(node)
  /** Copies the elements into a [List]. */
  fun toList(): List<A> = asSequence().toList()

  // Monoid
  override fun op(a: Chain<A>, b: Chain<A>): Chain<A> = a + b
  override fun mzero(): Chain<A> = empty()

  // Monad
  /** Applies [f] to every element. */
  infix fun <B> fmap(f: (A) -> B): Chain<B> = monad.fmap(f, this)
  /** Applies every function in [f] to every element. */
  infix fun <B> ap(f: K1<T, (A) -> B>): Chain<B> = monad.ap(f, this)
  /** Maps each element to a chain and concatenates the results. */
  infix fun <B> bind(f: (A) -> K1<T, B>): Chain<B> = monad.bind(f, this)

  // Foldable
  /** Folds the elements from the left. */
  fun <B> foldl(initial: B, f: (B, A) -> B): B = traversable.foldl(initial, f, this)
  /** Folds the elements from the right. */
  fun <B> foldr(initial: B, f: (A, B) -> B): B = traversable.foldr(initial, f, this)
  /** Maps each element into the Monoid [M] and combines the results. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = traversable.foldMap(type, f, this)

  // Traversable
  /** Applies an effectful [f] to each element and collects the results inside [applicative]. */
  fun <G, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse(applicative, f, this)

  // Filterable / Witherable
  /** Maps every element with [f], keeping only the [Maybe.Just] results. */
  infix fun <B> mapFilter(f: (A) -> Maybe<B>): Chain<B> = witherable.mapFilter(f, this)
  /** Keeps only the elements satisfying [p]. */
  infix fun filter(p: (A) -> Boolean): Chain<A> = witherable.filter(p, this)
  /** Maps every element with the partial function [f], dropping the elements it maps to `null`. */
  infix fun <B: Any> collect(f: (A) -> B?): Chain<B> = witherable.collect(f, this)
  /** Runs the effectful [f] on every element, keeping the [Maybe.Just] results inside the effect. */
  fun <G, B> wither(applicative: Applicative<G>, f: (A) -> K1<G, Maybe<B>>): K1<G, K1<T, B>> = witherable.wither(applicative, f, this)
  /** Keeps only the elements for which the effectful predicate [p] yields true. */
  fun <G> filterA(applicative: Applicative<G>, p: (A) -> K1<G, Boolean>): K1<G, K1<T, A>> = witherable.filterA(applicative, p, this)

  // Semialign / Zip
  /** Pairs this chain with [m] by index, keeping the leftover elements of the longer one. */
  infix fun <B> align(m: K1<T, B>): Chain<Ior<A, B>> = zip.align(this, m)
  /** Pairs this chain with [m] by index, truncating to the shorter one. */
  infix fun <B> zip(m: K1<T, B>): Chain<Pair<A, B>> = zip.zip(this, m)
  /** Combines this chain with [m] by index using [f], truncating to the shorter one. */
  fun <B, C> zipWith(m: K1<T, B>, f: (A, B) -> C): Chain<C> = zip.zipWith(f, this, m)

  override fun equals(other: Any?): Boolean =
    other is Chain<*> && size == other.size && asSequence().zip(other.asSequence()).all { (a, b) -> a == b }
  override fun hashCode(): Int = toList().hashCode()
  override fun toString(): String = joinToString(", ", "Chain(", ")")
}

/** Creates a [Chain] from the given values. */
fun <A> chainOf(vararg values: A): Chain<A> = Chain.fromList(values.toList())
/** Wraps this [List] in a [Chain] without copying it. */
fun <A> List<A>.toChain(): Chain<A> = Chain.fromList(this)
