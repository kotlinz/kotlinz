package com.github.unhappychoice.kotlinz.data.identity

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.monad.Monad

interface IdentityMonad: Monad<Identity.µ>, IdentityApplicative, IdentityFunctor {
  override fun <A> join(v: K1<Identity.µ, K1<Identity.µ, A>>): Identity<A> {
    return Identity.narrow(Identity.narrow(v).value)
  }

  override fun <A, B> bind(f: (A) -> K1<Identity.µ, B>, v: K1<Identity.µ, A>): Identity<B> {
    return Identity.narrow(super.bind(f, v))
  }
}
