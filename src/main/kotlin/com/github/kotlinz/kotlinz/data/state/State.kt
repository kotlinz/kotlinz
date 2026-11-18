package com.github.kotlinz.kotlinz.data.state

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

/**
 * A stateful computation: a function `S -> Pair<A, S>` that produces a value and a new state.
 *
 * Use it to thread a piece of state through a sequence of pure steps without passing it by hand.
 * Has Functor, Applicative, Monad, MonadState and MonadRec instances.
 * Deep `bind` chains are not stack-safe; use [StateMonadRec.tailRecM] for long loops.
 *
 * ```kotlin
 * val program = State.get<Int>() bind { n -> State.put(n + 1) bind { State.pure<Int, Int>(n) } }
 * program.run(5)  // Pair(5, 6)
 * program.eval(5) // 5
 * program.exec(5) // 6
 * ```
 *
 * @property run the state transition function.
 */
class State<S, A>(val run: (S) -> Pair<A, S>): K2<State.T, S, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded value to its concrete type. */
    fun <S, A> narrow(v: K1<K1<T, S>, A>) = v as State<S, A>

    // Monad
    /** Returns `v` without touching the state. */
    fun <S, A> pure(v: A): State<S, A> = monad<S>().pure(v)
    /** Flattens a nested State, running the outer then the inner computation. */
    fun <S, A> join(v: K1<K1<T, S>, K1<K1<T, S>, A>>): State<S, A> = monad<S>().join(v)
    private fun <S> monad() = object: StateMonad<S> {}
    /** Builds a State using monad comprehension syntax. */
    fun <S, A> fx(block: suspend MonadScope<K1<T, S>>.() -> A): State<S, A> = narrow(monad<S>().fx(block))

    // ApplicativeOps
    /** Lifts a unary function to operate on State values (applicative style). */
    fun <S, A, B> liftA(f: (A) -> B) = applicativeOps<S>().liftA(f)
    /** Lifts a binary function to operate on State values (applicative style). */
    fun <S, A, B, C> liftA2(f: (A, B) -> C) = applicativeOps<S>().liftA2(f)
    /** Lifts a ternary function to operate on State values (applicative style). */
    fun <S, A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps<S>().liftA3(f)
    private fun <S> applicativeOps() = object: StateApplicativeOps<S> {}

    // MonadOps
    /** Lifts a unary function to operate on State values (monadic style). */
    fun <S, A, B> liftM(f: (A) -> B) = monadOps<S>().liftM(f)
    /** Lifts a binary function to operate on State values (monadic style). */
    fun <S, A, B, C> liftM2(f: (A,  B) -> C) =  monadOps<S>().liftM2(f)
    /** Lifts a ternary function to operate on State values (monadic style). */
    fun <S, A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps<S>().liftM3(f)
    private fun <S> monadOps() = object: StateMonadOps<S> {}

    // MonadState
    /** Returns the current state as the value. */
    fun <S> get(): State<S, S> = monadState<S>().get()
    /** Replaces the state with `s`. */
    fun <S> put(s: S): State<S, Unit> = monadState<S>().put(s)
    /** Updates the state with `f`. */
    fun <S> modify(f: (S) -> S): State<S, Unit> = monadState<S>().modify(f)
    /** Returns a projection of the current state. */
    fun <S, A> gets(f: (S) -> A): State<S, A> = narrow(monadState<S>().gets(f))
    /** Wraps a raw state transition function. */
    fun <S, A> state(f: (S) -> Pair<A, S>): State<S, A> = monadState<S>().state(f)
    private fun <S> monadState() = object: StateMonadState<S> {}
  }

  // Monad
  /** Maps the produced value, leaving the state transition intact. */
  infix fun <B> fmap(f: (A) -> B): State<S, B> = monad.fmap(f, this)
  /** Runs `f` then this, applying the resulting function to the resulting value. */
  infix fun <B> ap(f: State<S, (A) -> B>): State<S, B> = monad.ap(f, this)
  /** Sequences this computation with `f`, threading the state. */
  infix fun <B> bind(f: (A) -> State<S, B>): State<S, B> = monad.bind(f, this)
  private val monad = object: StateMonad<S> {}

  /** Runs from initial state `s` and returns only the value. */
  fun eval(s: S): A = run(s).first
  /** Runs from initial state `s` and returns only the final state. */
  fun exec(s: S): S = run(s).second
}

/** Top-level alias of [State.get]: returns the current state. */
fun <S> get(): State<S, S> = State { Pair(it, it) }
/** Top-level alias of [State.put]: replaces the state with `s`. */
fun <S> put(s: S): State<S, Unit> = State { Pair(Unit, s) }
