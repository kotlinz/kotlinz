package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * An optional value: either [Just] holding a value or [None].
 *
 * Use it instead of nullable types when a computation may produce no result and you want to
 * compose such computations with `fmap`/`bind`, combine alternatives with `orElse`, or traverse them.
 *
 * Instances: Functor, Applicative, Monad, Alternative, MonadPlus, Foldable, Traversable, Semialign, Align, Zip.
 *
 * ```kotlin
 * val half = { n: Int -> if (n % 2 == 0) Maybe.Just(n / 2) else Maybe.None<Int>() }
 * Maybe.Just(8) bind half bind half // Just(2)
 * Maybe.Just(3) bind half           // None
 * Maybe.None<Int>().getOrElse(0)    // 0
 * ```
 */
sealed class Maybe<A>: K1<Maybe.T, A> {

  /** Witness type used to encode `Maybe` as a higher-kinded `K1<Maybe.T, A>`. */
  class T

  /** The absence of a value. */
  class None<A>: Maybe<A>() {
    override fun equals(other: Any?): Boolean = other is None<*>
    override fun hashCode(): Int = 0
    override fun toString(): String = "None"
  }
  /** A present [value]. */
  data class Just<A>(val value: A): Maybe<A>()

  companion object {
    /** Downcasts a higher-kinded `K1<Maybe.T, A>` back to [Maybe]. */
    fun <A> narrow(v: K1<T, A>): Maybe<A> = v as Maybe<A>

    // Monad
    /** Wraps [f] in [Just]. */
    fun <A> pure(f: A): Maybe<A> = monad.pure(f)
    /** Flattens a nested `Maybe<Maybe<A>>`. */
    fun <A> join(v: K1<T, K1<T, A>>): Maybe<A> = monad.join(v)
    private val monad = object: MaybeMonad {}
    /** Runs a monadic block where `bind()` short-circuits on [None]. */
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Maybe<A> = narrow(monad.fx(block))

    // ApplicativeOps
    /** Lifts a unary function to operate on `Maybe` values. */
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    /** Lifts a binary function to operate on `Maybe` values. */
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    /** Lifts a ternary function to operate on `Maybe` values. */
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: MaybeApplicativeOps {}

    // Alternative
    /** Returns [None], the identity of [orElse]. */
    fun <A> empty(): Maybe<A> = alternative.empty()
    private val alternative = object: MaybeAlternative {}

    // MonadPlus
    /** Returns [None], the identity of [mplus]. */
    fun <A> mzero(): Maybe<A> = monadPlus.mzero()
    private val monadPlus = object: MaybeMonadPlus {}

    // MonadOps
    /** Lifts a unary function into the Maybe monad. */
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    /** Lifts a binary function into the Maybe monad. */
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    /** Lifts a ternary function into the Maybe monad. */
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)
    private val monadOps = object: MaybeMonadOps {}

    // Traversable
    /** Turns a `Maybe` of `G` effects into a `G` effect of `Maybe`. */
    fun <G, A> sequence(applicative: Applicative<G>, v: K1<T, K1<G, A>>): K1<G, K1<T, A>> = traversable.sequence(applicative, v)
    private val traversable = object: MaybeTraversable {}

    // Align
    /** Returns [None], the identity of [align]. */
    fun <A> nil(): Maybe<A> = align.nil()
    private val align = object: MaybeAlign {}
  }

  // Monad
  /** Applies [f] to the value if present. */
  infix fun <B> fmap(f: (A) -> B): Maybe<B> = monad.fmap(f, this)
  /** Applies the function in [f] to the value if both are present. */
  infix fun <B> ap(f: K1<T, (A) -> B>): Maybe<B> = monad.ap(f, this)
  /** Chains a computation that may itself return [None]. */
  infix fun <B> bind(f: (A) -> K1<T, B>): Maybe<B> = monad.bind(f, this)
  private val monad = object: MaybeMonad {}

  // Foldable
  /** Left fold over zero or one element. */
  fun <B> foldl(initial: B, f: (B, A) -> B): B = foldable.foldl(initial, f, this)
  /** Right fold over zero or one element. */
  fun <B> foldr(initial: B, f: (A, B) -> B): B = foldable.foldr(initial, f, this)
  /** Maps the value (if any) into the Monoid [M], or returns its identity. */
  fun <M: Monoid<*, M>> foldMap(type: Class<M>, f: (A) -> M): M = foldable.foldMap(type, f, this)
  private val foldable = object: MaybeFoldable {}

  // Traversable
  /** Applies an effectful [f] to the value and collects the result inside [applicative]. */
  fun <G, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>): K1<G, K1<T, B>> = traversable.traverse(applicative, f, this)
  private val traversable = object: MaybeTraversable {}

  // Semialign / Zip
  /** Pairs this with [m], keeping whichever values are present. */
  infix fun <B> align(m: K1<T, B>): Maybe<Ior<A, B>> = zip.align(this, m)
  /** Combines this with [m] using [f] on whichever values are present. */
  fun <B, C> alignWith(m: K1<T, B>, f: (Ior<A, B>) -> C): Maybe<C> = zip.alignWith(f, this, m)
  /** Pairs this with [m] when both values are present. */
  infix fun <B> zip(m: K1<T, B>): Maybe<Pair<A, B>> = zip.zip(this, m)
  /** Combines this with [m] using [f] when both values are present. */
  fun <B, C> zipWith(m: K1<T, B>, f: (A, B) -> C): Maybe<C> = zip.zipWith(f, this, m)
  private val zip = object: MaybeZip {}

  /** Returns the contained value, or [v] when [None]. */
  fun getOrElse(v: A): A {
    return when (this) {
      is Just -> this.value
      is None -> v
    }
  }

  // Alternative
  /** Returns this if it is [Just], otherwise [m]. */
  infix fun orElse(m: K1<T, A>): Maybe<A> = alternative.orElse(this, m)
  /** Never fails: wraps this in [Just] (yielding `Just(None)` for [None]). */
  fun optional(): Maybe<Maybe<A>> = narrow(alternative.optional(this))
  private val alternative = object: MaybeAlternative {}

  // MonadPlus
  /** Same as [orElse]. */
  infix fun mplus(m: K1<T, A>): Maybe<A> = monadPlus.mplus(this, m)
  private val monadPlus = object: MaybeMonadPlus {}
}
