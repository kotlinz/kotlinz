package com.github.unhappychoice.kotlinz.data.state

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.Functor

interface StateFunctor<S>: Functor<K1<State.T, S>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<State.T, S>, A>): State<S, B> {
    return State { s ->
      val (value, s2) = State.narrow(v).value(s)
      Pair(f(value), s2)
    }
  }
}