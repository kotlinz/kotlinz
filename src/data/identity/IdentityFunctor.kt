package data.identity

import K1
import type.Functor

interface IdentityFunctor: Functor<Identity.µ> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Identity.µ, A>): Identity<B> {
    return Identity(f(Identity.narrow(v).value))
  }
}