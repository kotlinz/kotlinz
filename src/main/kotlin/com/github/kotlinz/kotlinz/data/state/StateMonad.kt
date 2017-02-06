package com.github.kotlinz.kotlinz.data.state

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

interface StateMonad<S>: Monad<K1<State.T, S>>, StateApplicative<S>, StateFunctor<S> {
  override fun <A> join(v: K1<K1<State.T, S>, K1<K1<State.T, S>, A>>): State<S, A> {
    val state = State.narrow(v)
    return State { s ->
      val (value, s2) = state.run(s)
      State.Companion.narrow(value).run(s2)
    }
  }

  override fun <A, B> bind(f: (A) -> K1<K1<State.T, S>, B>, v: K1<K1<State.T, S>, A>): State<S, B> {
    return State.narrow(super.bind(f, v))
  }
}