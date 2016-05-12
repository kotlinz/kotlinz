package com.github.unhappychoice.kotlinz.data.state

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.monad.Monad

interface StateMonad<S>: Monad<K1<State.T, S>>, StateApplicative<S>, StateFunctor<S> {
  override fun <A> join(v: K1<K1<State.T, S>, K1<K1<State.T, S>, A>>): State<S, A> {
    val state = State.narrow(v)
    return State { s ->
      val (value, s2) = state.value(s)
      State.narrow(value).value(s2)
    }
  }

  override fun <A, B> bind(f: (A) -> K1<K1<State.T, S>, B>, v: K1<K1<State.T, S>, A>): State<S, B> {
    return State.narrow(super.bind(f, v))
  }
}