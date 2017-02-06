package com.github.kotlinz.kotlinz.data.state

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface StateApplicative<S>: Applicative<K1<State.T, S>> {
  override fun <A> pure(v: A): State<S, A> = State { s -> Pair(v, s) }

  override fun <A, B> ap(f: K1<K1<State.T, S>, (A) -> B>, v: K1<K1<State.T, S>, A>): State<S, B> {
    val state = State.narrow(v)
    val statef = State.narrow(f)

    return State { s ->
      val (value, s2) = statef.run(s)
      State.narrow(fmap(value, state)).run(s2)
    }
  }

}