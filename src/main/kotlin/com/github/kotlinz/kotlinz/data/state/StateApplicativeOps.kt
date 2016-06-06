package com.github.kotlinz.kotlinz.data.state

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

interface StateApplicativeOps<S>: ApplicativeOps<K1<State.T, S>>, StateMonad<S> {
  override fun <A, B> liftA(f: (A) -> B): (K1<K1<State.T, S>, A>) -> State<S, B> {
    return { a -> State.narrow(a) ap State.pure(f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<K1<State.T, S>, A>, K1<K1<State.T, S>, B>) -> State<S, C> {
    return { a1, a2 ->
      val i1 = State.narrow(a1)
      val i2 = State.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<K1<State.T, S>, A>, K1<K1<State.T, S>, B>, K1<K1<State.T, S>, C>) -> State<S, D> {
    return { a1, a2, a3 ->
      val i1 = State.narrow(a1)
      val i2 = State.narrow(a2)
      val i3 = State.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
} 
