package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A higher-kinded wrapper around Kotlin's [List], modelling non-deterministic computations.
 *
 * Use it when you need a list as a `K1` to pass to generic type-class code, or want list-monad
 * style "all combinations" composition via `bind`.
 *
 * Instances: Functor, Applicative, Monad, Alternative, MonadPlus, MonadZip, Foldable, Traversable, Semialign, Align, Zip.
 *
 * ```kotlin
 * val pairs = listKOf(1, 2) bind { a -> listKOf("x", "y") fmap { b -> a to b } }
 * // ListK([(1, x), (1, y), (2, x), (2, y)])
 * listKOf(1, 2, 3).foldl(0) { acc, n -> acc + n } // 6
 * listKOf(1, 2) mzip listKOf("a", "b")             // ListK([(1, a), (2, b)])
 * ```
 */
data class ListK<A>(val value: List<A>): K1<ListK.T, A> {
  /** Witness type used to encode `ListK` as a higher-kinded `K1<ListK.T, A>`. */
  class T

  companion object {
    /** Downcasts a higher-kinded `K1<ListK.T, A>` back to [ListK]. */
    fun <A> narrow(v: K1<T, A>): ListK<A> = v as ListK<A>

    // Monad
    /** Creates a singleton list. */
    fun <A> pure(v: A): ListK<A> = monad.pure(v)
    /** Flattens a list of lists. */
    fun <A> join(v: K1<T, K1<T, A>>): ListK<A> = monad.join(v)
    private val monad = object: ListKMonad {}
    /**
     * Runs a monadic block over every combination of bound values.
     *
     * The block is replayed from the start for each element, so side effects between binds may run multiple times.
     */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): ListK<A> = narrow(monad.fxReplay(block))

    // ApplicativeOps
    /** Lifts a unary function to operate on `ListK` values. */
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    /** Lifts a binary function to operate on all combinations of two lists. */
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    /** Lifts a ternary function to operate on all combinations of three lists. */
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: ListKApplicativeOps {}

    // Alternative
    /** Returns the empty list. */
    fun <A> empty(): ListK<A> = alternative.empty()
    private val alternative = object: ListKAlternative {}

    // MonadOps
    /** Lifts a unary function into the list monad. */
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    /** Lifts a binary function into the list monad. */
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    /** Lifts a ternary function into the list monad. */
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)
    private val monadOps = object: ListKMonadOps {}

    // MonadPlus
    /** Returns the empty list. */
    fun <A> mzero(): ListK<A> = monadPlus.mzero()
    private val monadPlus = object: ListKMonadPlus {}

    // MonadZip
    /** Splits a list of pairs into a pair of lists. */
    fun <A, B> munzip(m: K1<T, Pair<A, B>>): Pair<ListK<A>, ListK<B>> = monadZip.munzip(m)
    private val monadZip = object: ListKMonadZip {}

    // Traversable
    /** Turns a list of `G` effects into a `G` effect of a list. */
    fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> = traversable.sequence(applicative, v)
    private val traversable = object: ListKTraversable {}

    // Align
    /** Returns the empty list, the identity of [align]. */
    fun <A> nil(): ListK<A> = align.nil()
    private val align = object: ListKAlign {}
  }

  // Monad
  /** Applies [f] to every element. */
  infix fun <B> fmap(f: (A) -> B): ListK<B> = monad.fmap(f, this)
  /** Applies every function in [f] to every element. */
  infix fun <B> ap(f: K1<T, (A) -> B>): ListK<B> = monad.ap(f, this)
  /** Maps each element to a list and concatenates the results. */
  infix fun <B> bind(f: (A) -> K1<T, B>): ListK<B> = monad.bind(f, this)
  private val monad = object: ListKMonad {}

  // Foldable
  /** Folds the elements from the left. */
  fun <B> foldl(initial: B, f: (B, A) -> B): B = foldable.foldl(initial, f, this)
  /** Folds the elements from the right. */
  fun <B> foldr(initial: B, f: (A, B) -> B): B = foldable.foldr(initial, f, this)
  /** Maps each element into the Monoid [M] and combines the results. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = foldable.foldMap(type, f, this)
  private val foldable = object: ListKFoldable {}

  // Traversable
  /** Applies an effectful [f] to each element and collects the results inside [applicative]. */
  fun <G, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse(applicative, f, this)
  private val traversable = object: ListKTraversable {}

  // MonadPlus
  /** Concatenates this list with [m]. */
  infix fun mplus(m: K1<T, A>): ListK<A> = monadPlus.mplus(this, m)
  private val monadPlus = object: ListKMonadPlus {}

  // Alternative
  /** Concatenates this list with [m]. */
  infix fun orElse(m: K1<T, A>): ListK<A> = alternative.orElse(this, m)
  /** Wraps each element in [Maybe.Just] and appends a single [Maybe.None]. */
  fun optional(): ListK<Maybe<A>> = narrow(alternative.optional(this))
  private val alternative = object: ListKAlternative {}

  // MonadZip
  /** Zips this list with [m], truncating to the shorter one. */
  infix fun <B> mzip(m: K1<T, B>): ListK<Pair<A, B>> = monadZip.mzip(this, m)
  /** Zips this list with [m] using [f], truncating to the shorter one. */
  fun <B, C> mzipWith(m: K1<T, B>, f: (A, B) -> C): ListK<C> = monadZip.mzipWith(this, m, f)
  private val monadZip = object: ListKMonadZip {}

  // Semialign / Zip
  /** Pairs this list with [m] by index, keeping the leftover elements of the longer one. */
  infix fun <B> align(m: K1<T, B>): ListK<Ior<A, B>> = zip.align(this, m)
  /** Combines this list with [m] by index using [f], keeping the leftover elements of the longer one. */
  fun <B, C> alignWith(m: K1<T, B>, f: (Ior<A, B>) -> C): ListK<C> = zip.alignWith(f, this, m)
  /** Pairs this list with [m] by index, truncating to the shorter one. */
  infix fun <B> zip(m: K1<T, B>): ListK<Pair<A, B>> = zip.zip(this, m)
  /** Combines this list with [m] by index using [f], truncating to the shorter one. */
  fun <B, C> zipWith(m: K1<T, B>, f: (A, B) -> C): ListK<C> = zip.zipWith(f, this, m)
  private val zip = object: ListKZip {}
}

/** Wraps this [List] in a [ListK]. */
fun <A> List<A>.toListK(): ListK<A> = ListK(this)
/** Creates a [ListK] from the given values. */
fun <A> listKOf(vararg values: A): ListK<A> = ListK(values.toList())
