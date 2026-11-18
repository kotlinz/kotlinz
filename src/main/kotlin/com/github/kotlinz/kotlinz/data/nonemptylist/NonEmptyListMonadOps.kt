package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadOps

/** MonadOps (liftM family) for [NonEmptyList]. */
interface NonEmptyListMonadOps: MonadOps<NonEmptyList.T>, NonEmptyListMonad {
  override fun <A, B> liftM(f: (A) -> B): (K1<NonEmptyList.T, A>) -> K1<NonEmptyList.T, B> {
    return { m ->
      val i = NonEmptyList.narrow(m)
      i bind { x -> NonEmptyList.pure(f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A,  B) -> C): (K1<NonEmptyList.T, A>, K1<NonEmptyList.T, B>) -> K1<NonEmptyList.T, C> {
    return { m1, m2 ->
      val i1 = NonEmptyList.narrow(m1)
      val i2 = NonEmptyList.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> NonEmptyList.pure(f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<NonEmptyList.T, A>, K1<NonEmptyList.T, B>, K1<NonEmptyList.T, C>) -> K1<NonEmptyList.T, D> {
    return { m1, m2, m3 ->
      val i1 = NonEmptyList.narrow(m1)
      val i2 = NonEmptyList.narrow(m2)
      val i3 = NonEmptyList.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> NonEmptyList.pure(f(x1, x2, x3)) } } }
    }
  }
}