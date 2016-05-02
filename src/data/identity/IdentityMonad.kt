package data.identity

import K1
import type.monad.Monad

interface IdentityMonad: Monad<Identity.µ> {
  override fun <A> pure(v: A): Identity<A> = Identity(v)

  override fun <A, B> fmap(f: (A) -> B, v: K1<Identity.µ, A>): Identity<B> {
    return Identity(f(Identity.narrow(v).value))
  }

  override fun <A, B> ap(f: K1<Identity.µ, (A) -> B>, v: K1<Identity.µ, A>): Identity<B> {
    return fmap(Identity.narrow(f).value, v)
  }

  override fun <A> join(v: K1<Identity.µ, K1<Identity.µ, A>>): Identity<A> {
    return Identity.narrow(Identity.narrow(v).value)
  }

  override fun <A, B> bind(f: (A) -> K1<Identity.µ, B>, v: K1<Identity.µ, A>): Identity<B> {
    return Identity.narrow(super.bind(f, v))
  }
}
