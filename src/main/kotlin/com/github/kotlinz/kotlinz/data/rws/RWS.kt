package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.K4
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Reader-Writer-State monad: a function `(R, S) -> Triple<A, S, W>` that reads an environment `R`,
 * threads a state `S` and accumulates a log `W`.
 *
 * Use it when a computation needs all three effects at once. `W` must be a [Monoid]; its runtime class is passed as [type].
 * Has instances for Functor, Applicative, Monad, MonadReader, MonadWriter and MonadState.
 *
 * ```kotlin
 * val type = StringMonoid::class.java
 * val program = RWS.ask<Int, StringMonoid, Int>(type) bind { r ->
 *   RWS.tell<Int, StringMonoid, Int>(type, StringMonoid("r=$r;")) bind {
 *     RWS.modify<Int, StringMonoid, Int>(type) { it + r } bind { RWS.get<Int, StringMonoid, Int>(type) }
 *   }
 * }
 * program.run(2, 10) // Triple(12, 12, StringMonoid("r=2;"))
 * ```
 */
class RWS<R, W: Monoid<*, W>, S, A>(val type: Class<W>, val run: (R, S) -> Triple<A, S, W>): K4<RWS.T, R, W, S, A> {
  /** Higher-kinded witness tag for [RWS]. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [RWS]. */
    fun <R, W: Monoid<*, W>, S, A> narrow(v: K1<K3<T, R, W, S>, A>) = v as RWS<R, W, S, A>

    // Monad
    /** Returns [v] without reading, logging or changing state. */
    fun <R, W: Monoid<*, W>, S, A> pure(type: Class<W>, v: A): RWS<R, W, S, A> = monad<R, W, S>(type).pure(v)
    /** Flattens a nested [RWS], threading state and combining logs. */
    fun <R, W: Monoid<*, W>, S, A> join(type: Class<W>, v: K1<K3<T, R, W, S>, K1<K3<T, R, W, S>, A>>): RWS<R, W, S, A> = monad<R, W, S>(type).join(v)
    private fun <R, W: Monoid<*, W>, S> monad(type: Class<W>) = object: RWSMonad<R, W, S> { override val type = type }
    /** Runs a monad comprehension in [RWS]. */
    fun <R, W: Monoid<*, W>, S, A> fx(type: Class<W>, block: suspend MonadScope<K3<T, R, W, S>>.() -> A): RWS<R, W, S, A> = narrow(monad<R, W, S>(type).fx(block))

    // ApplicativeOps
    /** Lifts a unary function into [RWS] using the Applicative instance. */
    fun <R, W: Monoid<*, W>, S, A, B> liftA(type: Class<W>, f: (A) -> B) = applicativeOps<R, W, S>(type).liftA(f)
    /** Lifts a binary function into [RWS] using the Applicative instance. */
    fun <R, W: Monoid<*, W>, S, A, B, C> liftA2(type: Class<W>, f: (A, B) -> C) = applicativeOps<R, W, S>(type).liftA2(f)
    /** Lifts a ternary function into [RWS] using the Applicative instance. */
    fun <R, W: Monoid<*, W>, S, A, B, C, D> liftA3(type: Class<W>, f: (A, B, C) -> D) = applicativeOps<R, W, S>(type).liftA3(f)
    private fun <R, W: Monoid<*, W>, S> applicativeOps(type: Class<W>) = object: RWSApplicativeOps<R, W, S> { override val type = type }

    // MonadOps
    /** Lifts a unary function into [RWS] using the Monad instance. */
    fun <R, W: Monoid<*, W>, S, A, B> liftM(type: Class<W>, f: (A) -> B) = monadOps<R, W, S>(type).liftM(f)
    /** Lifts a binary function into [RWS] using the Monad instance. */
    fun <R, W: Monoid<*, W>, S, A, B, C> liftM2(type: Class<W>, f: (A, B) -> C) = monadOps<R, W, S>(type).liftM2(f)
    /** Lifts a ternary function into [RWS] using the Monad instance. */
    fun <R, W: Monoid<*, W>, S, A, B, C, D> liftM3(type: Class<W>, f: (A, B, C) -> D) = monadOps<R, W, S>(type).liftM3(f)
    private fun <R, W: Monoid<*, W>, S> monadOps(type: Class<W>) = object: RWSMonadOps<R, W, S> { override val type = type }

    // Primitives
    /** Returns the environment. */
    fun <R, W: Monoid<*, W>, S> ask(type: Class<W>): RWS<R, W, S, R> = RWS(type) { r, s -> Triple(r, s, Monoid.mzero(type)) }
    /** Appends [w] to the log. */
    fun <R, W: Monoid<*, W>, S> tell(type: Class<W>, w: W): RWS<R, W, S, Unit> = RWS(type) { r, s -> Triple(Unit, s, w) }
    /** Returns the current state. */
    fun <R, W: Monoid<*, W>, S> get(type: Class<W>): RWS<R, W, S, S> = RWS(type) { r, s -> Triple(s, s, Monoid.mzero(type)) }
    /** Replaces the state with [s]. */
    fun <R, W: Monoid<*, W>, S> put(type: Class<W>, s: S): RWS<R, W, S, Unit> = RWS(type) { r, _s -> Triple(Unit, s, Monoid.mzero(type)) }
    /** Updates the state with [f]. */
    fun <R, W: Monoid<*, W>, S> modify(type: Class<W>, f: (S) -> S): RWS<R, W, S, Unit> = RWS(type) { r, s -> Triple(Unit, f(s), Monoid.mzero(type)) }

    // MonadWriter
    /** Returns the value of [v] and appends its log. */
    fun <R, W: Monoid<*, W>, S, A> writer(type: Class<W>, v: Pair<A, W>): RWS<R, W, S, A> = monadWriter<R, W, S>(type).writer(v)
    private fun <R, W: Monoid<*, W>, S> monadWriter(type: Class<W>) = object: RWSMonadWriter<R, W, S> { override val type = type }

    // MonadState
    /** Embeds a state transition function. */
    fun <R, W: Monoid<*, W>, S, A> state(type: Class<W>, f: (S) -> Pair<A, S>): RWS<R, W, S, A> = monadState<R, W, S>(type).state(f)
    private fun <R, W: Monoid<*, W>, S> monadState(type: Class<W>) = object: RWSMonadState<R, W, S> { override val type = type }

    // MonadReader
    /** Creates an [RWS] from a function of the environment. */
    fun <R, W: Monoid<*, W>, S, A> reader(type: Class<W>, f: (R) -> A): RWS<R, W, S, A> = narrow(monadReader<R, W, S>(type).reader(f))
    private fun <R, W: Monoid<*, W>, S> monadReader(type: Class<W>) = object: RWSMonadReader<R, W, S> { override val type = type }
  }

  // Monad
  /** Maps the result value with [f]. */
  infix fun <B> fmap(f: (A) -> B): RWS<R, W, S, B> = monad.fmap(f, this)
  /** Applies the function produced by [f] to the result value. */
  infix fun <B> ap(f: RWS<R, W, S, (A) -> B>): RWS<R, W, S, B> = monad.ap(f, this)
  /** Sequences this computation with one that depends on its result. */
  infix fun <B> bind(f: (A) -> RWS<R, W, S, B>): RWS<R, W, S, B> = monad.bind(f, this)
  private val monad = object: RWSMonad<R, W, S> { override val type = this@RWS.type }

  /** Runs this computation with an environment modified by [f]. */
  fun local(f: (R) -> R): RWS<R, W, S, A> = RWS(type) { r, s -> run(f(r), s) }
  /** Runs the computation and returns the result value and the log. */
  fun eval(r: R, s: S): Pair<A, W> = run(r, s).let { Pair(it.first, it.third) }
  /** Runs the computation and returns the final state and the log. */
  fun exec(r: R, s: S): Pair<S, W> = run(r, s).let { Pair(it.second, it.third) }
}
