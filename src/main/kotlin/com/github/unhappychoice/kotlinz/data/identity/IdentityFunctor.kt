package com.github.unhappychoice.kotlinz.data.identity

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.Functor

interface IdentityFunctor: Functor<Identity.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Identity.T, A>): Identity<B> {
    return Identity(f(Identity.narrow(v).value))
  }
}