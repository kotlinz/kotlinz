package data.identity

import K1
import type.applicative.Applicative

interface IdentityApplicative: Applicative<Identity.µ> {
  override fun <A> pure(v: A): Identity<A> = Identity(v)

  override fun <A, B> ap(f: K1<Identity.µ, (A) -> B>, v: K1<Identity.µ, A>): Identity<B> {
    return Identity.narrow(fmap(Identity.narrow(f).value, v))
  }
}
