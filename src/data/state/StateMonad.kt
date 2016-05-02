package data.state

import K1
import type.monad.Monad

interface StateMonad<S>: Monad<K1<State.µ, S>> {
  override fun <A> pure(v: A): State<S, A> = State { s -> Pair(v, s) }

  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<State.µ, S>, A>): State<S, B> {
    return State { s ->
      val (value, s2) = State.narrow(v).value(s)
      Pair(f(value), s2)
    }
  }

  override fun <A, B> ap(f: K1<K1<State.µ, S>, (A) -> B>, v: K1<K1<State.µ, S>, A>): State<S, B> {
    val state = State.narrow(v)
    val statef = State.narrow(f)

    return State { s ->
      val (value, s2) = statef.value(s)
      fmap(value, state).value(s2)
    }
  }

  override fun <A> join(v: K1<K1<State.µ, S>, K1<K1<State.µ, S>, A>>): State<S, A> {
    val state = State.narrow(v)
    return State { s ->
      val (value, s2) = state.value(s)
      State.narrow(value).value(s2)
    }
  }

  override fun <A, B> bind(f: (A) -> K1<K1<State.µ, S>, B>, v: K1<K1<State.µ, S>, A>): State<S, B> {
    return State.narrow(super.bind(f, v))
  }
}