package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface MaybeFunctor: Functor<Maybe.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Maybe.T, A>): Maybe<B> {
    val maybe = Maybe.narrow(v)
    return when (maybe) {
      is Maybe.Just -> Maybe.Just(f(maybe.value))
      is Maybe.None -> Maybe.None()
    }
  }
}
