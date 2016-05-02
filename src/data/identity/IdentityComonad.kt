package data.identity

import K1
import type.Comonad

interface IdentityComonad: Comonad<Identity.µ> {
  // FIXME: implement duplication
  override fun <A, B> fmap(f: (A) -> B, v: K1<Identity.µ, A>): Identity<B> {
    return Identity(f(Identity.narrow(v).value))
  }

  override fun <A> extract(v: K1<Identity.µ, A>): A = Identity.narrow(v).value

  override fun <A> duplicate(v: K1<Identity.µ, A>): Identity<K1<Identity.µ, A>> {
    return Identity(Identity.narrow(v))
  }

  override fun <A, B> extend(f: (K1<Identity.µ, A>) -> B, v: K1<Identity.µ, A>): Identity<B> {
    return Identity.narrow(fmap(f, duplicate(v)))
  }
}
