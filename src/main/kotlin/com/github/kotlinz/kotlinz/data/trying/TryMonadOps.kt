package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadOps

/** Monadic lifting helpers for [Try]. */
interface TryMonadOps: MonadOps<Try.T>, TryMonad {
  override fun <A, B> liftM(f: (A) -> B): (K1<Try.T, A>) -> K1<Try.T, B> {
    return { m ->
      val i = Try.narrow(m)
      i bind { x -> Try.pure(f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A,  B) -> C): (K1<Try.T, A>, K1<Try.T, B>) -> K1<Try.T, C> {
    return { m1, m2 ->
      val i1 = Try.narrow(m1)
      val i2 = Try.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> Try.pure(f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<Try.T, A>, K1<Try.T, B>, K1<Try.T, C>) -> K1<Try.T, D> {
    return { m1, m2, m3 ->
      val i1 = Try.narrow(m1)
      val i2 = Try.narrow(m2)
      val i3 = Try.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> Try.pure(f(x1, x2, x3)) } } }
    }
  }
}