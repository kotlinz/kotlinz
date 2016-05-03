package data.identity

import K1
import type.monad.Monad

interface IdentityMonad: Monad<Identity.µ>, IdentityApplicative, IdentityFunctor {
  override fun <A> join(v: K1<Identity.µ, K1<Identity.µ, A>>): Identity<A> {
    return Identity.narrow(Identity.narrow(v).value)
  }

  override fun <A, B> bind(f: (A) -> K1<Identity.µ, B>, v: K1<Identity.µ, A>): Identity<B> {
    return Identity.narrow(super.bind(f, v))
  }
}
