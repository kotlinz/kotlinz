package com.github.unhappychoice.kotlinz.data.identity

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.Functor

interface IdentityFunctor: Functor<Identity.µ> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Identity.µ, A>): Identity<B> {
    return Identity(f(Identity.narrow(v).value))
  }
}