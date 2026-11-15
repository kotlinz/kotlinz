package com.github.kotlinz.kotlinz.data.io

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadOps

interface IOMonadOps: MonadOps<IO.T>, IOMonad {
  override fun <A, B> liftM(f: (A) -> B): (K1<IO.T, A>) -> K1<IO.T, B> {
    return { m ->
      val i = IO.narrow(m)
      i bind { x -> IO.pure(f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A, B) -> C): (K1<IO.T, A>, K1<IO.T, B>) -> K1<IO.T, C> {
    return { m1, m2 ->
      val i1 = IO.narrow(m1)
      val i2 = IO.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> IO.pure(f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<IO.T, A>, K1<IO.T, B>, K1<IO.T, C>) -> K1<IO.T, D> {
    return { m1, m2, m3 ->
      val i1 = IO.narrow(m1)
      val i2 = IO.narrow(m2)
      val i3 = IO.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> IO.pure(f(x1, x2, x3)) } } }
    }
  }
}
