package com.github.kotlinz.kotlinz.data.cont

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

interface ContApplicativeOps<R>: ApplicativeOps<K1<Cont.T, R>>, ContMonad<R> {
  override fun <A, B> liftA(f: (A) -> B): (K1<K1<Cont.T, R>, A>) -> Cont<R, B> {
    return { a -> Cont.narrow(a) ap Cont.pure(f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<K1<Cont.T, R>, A>, K1<K1<Cont.T, R>, B>) -> Cont<R, C> {
    return { a1, a2 ->
      val i1 = Cont.narrow(a1)
      val i2 = Cont.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<K1<Cont.T, R>, A>, K1<K1<Cont.T, R>, B>, K1<K1<Cont.T, R>, C>) -> Cont<R, D> {
    return { a1, a2, a3 ->
      val i1 = Cont.narrow(a1)
      val i2 = Cont.narrow(a2)
      val i3 = Cont.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
} 
