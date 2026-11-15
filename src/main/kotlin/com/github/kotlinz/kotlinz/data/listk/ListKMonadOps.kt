package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadOps

interface ListKMonadOps: MonadOps<ListK.T>, ListKMonad {
  override fun <A, B> liftM(f: (A) -> B): (K1<ListK.T, A>) -> K1<ListK.T, B> {
    return { m ->
      val i = ListK.narrow(m)
      i bind { x -> ListK.pure(f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A,  B) -> C): (K1<ListK.T, A>, K1<ListK.T, B>) -> K1<ListK.T, C> {
    return { m1, m2 ->
      val i1 = ListK.narrow(m1)
      val i2 = ListK.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> ListK.pure(f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<ListK.T, A>, K1<ListK.T, B>, K1<ListK.T, C>) -> K1<ListK.T, D> {
    return { m1, m2, m3 ->
      val i1 = ListK.narrow(m1)
      val i2 = ListK.narrow(m2)
      val i3 = ListK.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> ListK.pure(f(x1, x2, x3)) } } }
    }
  }
}