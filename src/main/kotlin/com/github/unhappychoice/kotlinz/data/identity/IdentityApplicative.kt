package com.github.unhappychoice.kotlinz.data.identity

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.applicative.Applicative

interface IdentityApplicative: Applicative<Identity.µ> {
  override fun <A> pure(v: A): Identity<A> = Identity(v)

  override fun <A, B> ap(f: K1<Identity.µ, (A) -> B>, v: K1<Identity.µ, A>): Identity<B> {
    return Identity.narrow(fmap(Identity.narrow(f).value, v))
  }
}
