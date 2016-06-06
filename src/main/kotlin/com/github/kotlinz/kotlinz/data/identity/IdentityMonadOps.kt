package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadOps

interface IdentityMonadOps: MonadOps<Identity.T>, IdentityMonad {
  override fun <A, B> liftM(f: (A) -> B): (K1<Identity.T, A>) -> K1<Identity.T, B> {
    return { m ->
      val i = Identity.narrow(m)
      i bind { x -> Identity.pure(f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A,  B) -> C): (K1<Identity.T, A>, K1<Identity.T, B>) -> K1<Identity.T, C> {
    return { m1, m2 ->
      val i1 = Identity.narrow(m1)
      val i2 = Identity.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> Identity.pure(f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<Identity.T, A>, K1<Identity.T, B>, K1<Identity.T, C>) -> K1<Identity.T, D> {
    return { m1, m2, m3 ->
      val i1 = Identity.narrow(m1)
      val i2 = Identity.narrow(m2)
      val i3 = Identity.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> Identity.pure(f(x1, x2, x3)) } } }
    }
  }
}