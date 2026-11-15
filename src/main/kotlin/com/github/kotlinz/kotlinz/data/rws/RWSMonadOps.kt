package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadOps

interface RWSMonadOps<R, W: Monoid<*, W>, S>: MonadOps<K3<RWS.T, R, W, S>>, RWSMonad<R, W, S> {
  override fun <A, B> liftM(f: (A) -> B): (K1<K3<RWS.T, R, W, S>, A>) -> K1<K3<RWS.T, R, W, S>, B> {
    return { m ->
      val i = RWS.narrow(m)
      i bind { x -> RWS.pure<R, W, S, B>(type, f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A, B) -> C): (K1<K3<RWS.T, R, W, S>, A>, K1<K3<RWS.T, R, W, S>, B>) -> K1<K3<RWS.T, R, W, S>, C> {
    return { m1, m2 ->
      val i1 = RWS.narrow(m1)
      val i2 = RWS.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> RWS.pure<R, W, S, C>(type, f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<K3<RWS.T, R, W, S>, A>, K1<K3<RWS.T, R, W, S>, B>, K1<K3<RWS.T, R, W, S>, C>) -> K1<K3<RWS.T, R, W, S>, D> {
    return { m1, m2, m3 ->
      val i1 = RWS.narrow(m1)
      val i2 = RWS.narrow(m2)
      val i3 = RWS.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> RWS.pure<R, W, S, D>(type, f(x1, x2, x3)) } } }
    }
  }
}
