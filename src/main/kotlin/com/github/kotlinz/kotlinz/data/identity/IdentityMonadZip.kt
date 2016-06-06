package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadZip

interface IdentityMonadZip: MonadZip<Identity.T>, IdentityMonad {
  override fun <A, B> mzip(m1: K1<Identity.T, A>, m2: K1<Identity.T, B>): Identity<Pair<A, B>> {
    return Identity.pure(Pair(Identity.narrow(m1).value, Identity.narrow(m2).value))
  }
  override fun <A, B, C> mzipWith(m1: K1<Identity.T, A>, m2: K1<Identity.T, B>, f: (A,  B) -> C): Identity<C> {
    val a1 = Identity.narrow(m1)
    val a2 = Identity.narrow(m2)
    return Identity.pure(f(a1.value, a2.value))
  }

  override fun <A, B> munzip(m: K1<Identity.T, Pair<A, B>>): Pair<Identity<A>, Identity<B>> {
    val a = Identity.narrow(m).value
    return Pair(Identity.pure(a.first), Identity.pure(a.second))
  }
}