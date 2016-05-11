package com.github.unhappychoice.kotlinz.data.maybe

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.Functor

interface MaybeFunctor: Functor<Maybe.µ> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Maybe.µ, A>): Maybe<B> {
    val maybe = Maybe.narrow(v)
    return when (maybe) {
      is Maybe.Just -> Maybe.Just(f(maybe.value))
      is Maybe.None -> Maybe.None()
    }
  }
}
