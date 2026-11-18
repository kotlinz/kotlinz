package com.github.kotlinz.kotlinz.data.rws

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps
import com.github.kotlinz.kotlinz.type.group.Monoid

/** ApplicativeOps (liftA family) for [RWS]. */
interface RWSApplicativeOps<R, W: Monoid<*, W>, S>: ApplicativeOps<K3<RWS.T, R, W, S>>, RWSMonad<R, W, S> {
  override fun <A, B> liftA(f: (A) -> B): (K1<K3<RWS.T, R, W, S>, A>) -> RWS<R, W, S, B> {
    return { a -> RWS.narrow(a) ap RWS.pure(type, f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<K3<RWS.T, R, W, S>, A>, K1<K3<RWS.T, R, W, S>, B>) -> RWS<R, W, S, C> {
    return { a1, a2 ->
      val i1 = RWS.narrow(a1)
      val i2 = RWS.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<K3<RWS.T, R, W, S>, A>, K1<K3<RWS.T, R, W, S>, B>, K1<K3<RWS.T, R, W, S>, C>) -> RWS<R, W, S, D> {
    return { a1, a2, a3 ->
      val i1 = RWS.narrow(a1)
      val i2 = RWS.narrow(a2)
      val i3 = RWS.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
}
