package com.github.kotlinz.kotlinz.data.cont

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadOps

/** MonadOps instance for [Cont]. */
interface ContMonadOps<R>: MonadOps<K1<Cont.T, R>>, ContMonad<R> {
  override fun <A, B> liftM(f: (A) -> B): (K1<K1<Cont.T, R>, A>) -> K1<K1<Cont.T, R>, B> {
    return { m ->
      val i = Cont.narrow(m)
      i bind { x -> Cont.pure<R, B>(f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A,  B) -> C): (K1<K1<Cont.T, R>, A>, K1<K1<Cont.T, R>, B>) -> K1<K1<Cont.T, R>, C> {
    return { m1, m2 ->
      val i1 = Cont.narrow(m1)
      val i2 = Cont.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> Cont.pure<R, C>(f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<K1<Cont.T, R>, A>, K1<K1<Cont.T, R>, B>, K1<K1<Cont.T, R>, C>) -> K1<K1<Cont.T, R>, D> {
    return { m1, m2, m3 ->
      val i1 = Cont.narrow(m1)
      val i2 = Cont.narrow(m2)
      val i3 = Cont.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> Cont.pure<R, D>(f(x1, x2, x3)) } } }
    }
  }
}