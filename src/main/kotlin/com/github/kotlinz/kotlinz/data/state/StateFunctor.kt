package com.github.kotlinz.kotlinz.data.state

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface StateFunctor<S>: Functor<K1<State.T, S>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<State.T, S>, A>): State<S, B> {
    return State { s ->
      val (value, s2) = State.narrow(v).run(s)
      Pair(f(value), s2)
    }
  }
}