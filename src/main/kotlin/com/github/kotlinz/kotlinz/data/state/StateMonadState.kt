package com.github.kotlinz.kotlinz.data.state

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadState

/** MonadState instance for [State]. */
interface StateMonadState<S>: MonadState<K1<State.T, S>, S>, StateMonad<S> {
  override fun get(): State<S, S> = State { s -> Pair(s, s) }
  override fun put(s: S): State<S, Unit> = State { Pair(Unit, s) }
  override fun modify(f: (S) -> S): State<S, Unit> = State { s -> Pair(Unit, f(s)) }
  override fun <A> state(f: (S) -> Pair<A, S>): State<S, A> = State(f)
}
