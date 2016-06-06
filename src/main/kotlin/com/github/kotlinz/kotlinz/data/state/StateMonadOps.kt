package com.github.kotlinz.kotlinz.data.state

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadOps

interface StateMonadOps<S>: MonadOps<K1<State.T, S>>, StateMonad<S> {
  override fun <A, B> liftM(f: (A) -> B): (K1<K1<State.T, S>, A>) -> K1<K1<State.T, S>, B> {
    return { m ->
      val i = State.narrow(m)
      i bind { x -> State.pure<S, B>(f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A,  B) -> C): (K1<K1<State.T, S>, A>, K1<K1<State.T, S>, B>) -> K1<K1<State.T, S>, C> {
    return { m1, m2 ->
      val i1 = State.narrow(m1)
      val i2 = State.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> State.pure<S, C>(f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<K1<State.T, S>, A>, K1<K1<State.T, S>, B>, K1<K1<State.T, S>, C>) -> K1<K1<State.T, S>, D> {
    return { m1, m2, m3 ->
      val i1 = State.narrow(m1)
      val i2 = State.narrow(m2)
      val i3 = State.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> State.pure<S, D>(f(x1, x2, x3)) } } }
    }
  }
}