package com.github.kotlinz.kotlinz.data.indexedstate

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

/**
 * An indexed state computation `I -> Pair<A, O>` whose state type may change from `I` to `O`.
 *
 * Use it to track protocol steps in the state type (e.g. `Unopened -> Opened -> Closed`) so that
 * out-of-order steps fail to compile. Instances: [IndexedStateIxFunctor], [IndexedStateIxApplicative],
 * [IndexedStateIxMonad], plus [IndexedStateMonad] when the state type does not change.
 *
 * ```kotlin
 * val program = IndexedState.get<Int>() bind { n -> IndexedState.put<Int, String>("n=$n") fmap { n } }
 * program.run(1) // Pair(1, "n=1")
 * ```
 *
 * @property run the state transition function.
 */
class IndexedState<I, O, A>(val run: (I) -> Pair<A, O>): K3<IndexedState.T, I, O, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded value to its concrete type. */
    fun <I, O, A> narrow(v: K1<K2<T, I, O>, A>): IndexedState<I, O, A> = v as IndexedState<I, O, A>

    /** Returns `v` without touching the state. */
    fun <S, A> pure(v: A): IndexedState<S, S, A> = IndexedState { s -> Pair(v, s) }
    /** Returns the current state as the value. */
    fun <S> get(): IndexedState<S, S, S> = IndexedState { s -> Pair(s, s) }
    /** Returns a projection of the current state. */
    fun <S, A> gets(f: (S) -> A): IndexedState<S, S, A> = IndexedState { s -> Pair(f(s), s) }
    /** Replaces the state with `o`, possibly of a different type. */
    fun <I, O> put(o: O): IndexedState<I, O, Unit> = IndexedState { Pair(Unit, o) }
    /** Transforms the state with `f`, possibly changing its type. */
    fun <I, O> modify(f: (I) -> O): IndexedState<I, O, Unit> = IndexedState { i -> Pair(Unit, f(i)) }
    /** Builds a state-preserving computation with monad comprehension syntax. */
    fun <S, A> fx(block: suspend MonadScope<K2<T, S, S>>.() -> A): IndexedState<S, S, A> = narrow(IndexedStateMonad.of<S>().fx(block))
  }

  /** Maps the produced value. */
  infix fun <B> fmap(f: (A) -> B): IndexedState<I, O, B> = monad.imap(f, this)
  /** Runs `f`, then this, applying the resulting function to the resulting value. */
  infix fun <I0, B> ap(f: IndexedState<I0, I, (A) -> B>): IndexedState<I0, O, B> = monad.iap(f, this)
  /** Sequences this computation with `f`, whose input state is this computation's output state. */
  infix fun <O2, B> bind(f: (A) -> IndexedState<O, O2, B>): IndexedState<I, O2, B> = monad.ibind(f, this)
  private val monad get() = object: IndexedStateIxMonad {}

  /** Runs from initial state `i` and returns only the value. */
  fun eval(i: I): A = run(i).first
  /** Runs from initial state `i` and returns only the final state. */
  fun exec(i: I): O = run(i).second
}

/** Converts a state-preserving [IndexedState] into a [State]. */
fun <S, A> IndexedState<S, S, A>.toState(): State<S, A> = State(run)
