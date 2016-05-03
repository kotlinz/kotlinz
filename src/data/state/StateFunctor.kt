package data.state

import K1
import type.Functor

interface StateFunctor<S>: Functor<K1<State.µ, S>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<State.µ, S>, A>): State<S, B> {
    return State { s ->
      val (value, s2) = State.narrow(v).value(s)
      Pair(f(value), s2)
    }
  }
}