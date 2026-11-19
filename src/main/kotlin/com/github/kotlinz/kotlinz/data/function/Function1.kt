package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

/**
 * A wrapped single-argument function `(A) -> B`.
 *
 * Lets plain functions participate in type class abstractions: with the input fixed it is
 * the reader monad, and as a two-parameter type it is an arrow and a profunctor.
 * Encoded as K3 with a phantom `Unit` so that it fits `Category<T, F>` / `Arrow<T, F>`.
 *
 * Instances: [Function1Functor], [Function1Applicative], [Function1ApplicativeOps],
 * [Function1Monad], [Function1MonadOps], [Function1Compose], [Function1Category],
 * [Function1Arrow], [Function1Profunctor], [Function1Strong], [Function1Choice],
 * [Function1Closed], [Function1Traversing], [Function1Cochoice].
 *
 * ```kotlin
 * val m = Function1 { r: Int -> r + 3 }
 * (m andThen Function1 { x: Int -> x * 10 })(1) // 40
 * (m combine Function1 { r: Int -> r * 2 })(5) // Pair(8, 10)
 * Function1.fx<Int, Int> { m.bind() * Function1 { r: Int -> r }.bind() }(2) // 10
 * ```
 */
class Function1<A, B>(val run: (A) -> B): K3<Function1.T, Unit, A, B> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Function1]. */
    fun <A, B> narrow(v: K1<K2<T, Unit, A>, B>): Function1<A, B> = v as Function1<A, B>

    // Monad
    /** Returns a constant function ignoring its input. */
    fun <R, A> pure(v: A): Function1<R, A> = monad<R>().pure(v)
    /** Flattens a nested function, feeding the same input to both layers. */
    fun <R, A> join(v: K1<K2<T, Unit, R>, K1<K2<T, Unit, R>, A>>): Function1<R, A> = monad<R>().join(v)
    private fun <R> monad() = object: Function1Monad<R> {}
    /** Builds a function with monad comprehension syntax; every bound function receives the same input. */
    fun <R, A> fx(block: suspend MonadScope<K2<T, Unit, R>>.() -> A): Function1<R, A> = narrow(monad<R>().fx(block))

    // ApplicativeOps
    /** Lifts a unary function to operate on [Function1] values. */
    fun <R, A, B> liftA(f: (A) -> B) = applicativeOps<R>().liftA(f)
    /** Lifts a binary function to operate on [Function1] values. */
    fun <R, A, B, C> liftA2(f: (A, B) -> C) = applicativeOps<R>().liftA2(f)
    /** Lifts a ternary function to operate on [Function1] values. */
    fun <R, A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps<R>().liftA3(f)
    private fun <R> applicativeOps() = object: Function1ApplicativeOps<R> {}

    // MonadOps
    /** Lifts a unary function using monadic bind. */
    fun <R, A, B> liftM(f: (A) -> B) = monadOps<R>().liftM(f)
    /** Lifts a binary function using monadic bind. */
    fun <R, A, B, C> liftM2(f: (A, B) -> C) = monadOps<R>().liftM2(f)
    /** Lifts a ternary function using monadic bind. */
    fun <R, A, B, C, D> liftM3(f: (A, B, C) -> D) = monadOps<R>().liftM3(f)
    private fun <R> monadOps() = object: Function1MonadOps<R> {}

    // Arrow
    /** The identity function. */
    fun <A> id(): Function1<A, A> = arrow.id()
    /** Lifts a plain Kotlin function into [Function1]. */
    fun <A, B> arr(f: (A) -> B): Function1<A, B> = arrow.arr(f)
    private val arrow = object: Function1Arrow {}
  }

  /** Applies the function to [a]. */
  operator fun invoke(a: A): B = run(a)

  // Monad
  /** Post-composes [f] onto this function. */
  infix fun <C> fmap(f: (B) -> C): Function1<A, C> = monad.fmap(f, this)
  /** Applies the function produced by [f] to the result, passing the same input to both. */
  infix fun <C> ap(f: Function1<A, (B) -> C>): Function1<A, C> = monad.ap(f, this)
  /** Sequences with [f], passing the same input to both functions. */
  infix fun <C> bind(f: (B) -> Function1<A, C>): Function1<A, C> = monad.bind(f, this)
  private val monad = object: Function1Monad<A> {}

  // Arrow
  /** Returns `this ∘ g`: runs [g] first, then this function. */
  infix fun <C> compose(g: Function1<C, A>): Function1<C, B> = arrow.compose(this, g)
  /** Runs this function first, then [g]. */
  infix fun <C> andThen(g: Function1<B, C>): Function1<A, C> = arrow.compose(g, this)
  /** Applies this function to the first component of a pair. */
  fun <C> first(): Function1<Pair<A, C>, Pair<B, C>> = arrow.first(this)
  /** Applies this function to the second component of a pair. */
  fun <C> second(): Function1<Pair<C, A>, Pair<C, B>> = narrow(arrow.second(this))
  /** Applies this function and [g] to the respective components of a pair. */
  infix fun <C, D> split(g: Function1<C, D>): Function1<Pair<A, C>, Pair<B, D>> = narrow(arrow.split(this, g))
  /** Feeds the same input to this function and [g], pairing the results. */
  infix fun <C> combine(g: Function1<A, C>): Function1<A, Pair<B, C>> = narrow(arrow.combine(this, g))
  private val arrow = object: Function1Arrow {}

  // Profunctor
  /** Pre-composes [f] and post-composes [g]. */
  fun <C, D> dimap(f: (C) -> A, g: (B) -> D): Function1<C, D> = profunctor.dimap(f, g, this)
  /** Pre-composes [f] onto the input. */
  infix fun <C> lmap(f: (C) -> A): Function1<C, B> = profunctor.lmap(f, this)
  /** Post-composes [g] onto the output. */
  infix fun <C> rmap(g: (B) -> C): Function1<A, C> = profunctor.rmap(g, this)
  private val profunctor = object: Function1Profunctor {}
}
