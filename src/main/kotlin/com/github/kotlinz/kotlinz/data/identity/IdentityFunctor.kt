package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface IdentityFunctor: Functor<Identity.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Identity.T, A>): Identity<B> {
    return Identity(f(Identity.Companion.narrow(v).value))
  }
}