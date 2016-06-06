package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

interface MaybeMonad: Monad<Maybe.T>, MaybeApplicative, MaybeFunctor {
  override fun <A> join(v: K1<Maybe.T, K1<Maybe.T, A>>): Maybe<A> {
    val maybe = Maybe.narrow(v)
    return when (maybe) {
      is Maybe.Just -> Maybe.narrow(maybe.value)
      is Maybe.None -> Maybe.None()
    }
  }

  override fun <A, B> bind(f: (A) -> K1<Maybe.T, B>, v: K1<Maybe.T, A>): Maybe<B> {
    return Maybe.narrow(super.bind(f, v))
  }
}