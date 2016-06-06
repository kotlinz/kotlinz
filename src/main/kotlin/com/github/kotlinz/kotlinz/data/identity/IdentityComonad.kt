package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Comonad

interface IdentityComonad: Comonad<Identity.T>, IdentityFunctor {
  override fun <A> extract(v: K1<Identity.T, A>): A = Identity.narrow(v).value

  override fun <A> duplicate(v: K1<Identity.T, A>): Identity<K1<Identity.T, A>> {
    return Identity(Identity.Companion.narrow(v))
  }

  override fun <A, B> extend(f: (K1<Identity.T, A>) -> B, v: K1<Identity.T, A>): Identity<B> {
    return Identity.narrow(fmap(f, duplicate(v)))
  }
}
