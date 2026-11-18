package com.github.kotlinz.kotlinz.data.io

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

/** Derived applicative helpers (liftA..liftA3) for [IO]. */
interface IOApplicativeOps: ApplicativeOps<IO.T>, IOMonad {
  override fun <A, B> liftA(f: (A) -> B): (K1<IO.T, A>) -> IO<B> {
    return { a -> IO.narrow(a) ap IO.pure(f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<IO.T, A>, K1<IO.T, B>) -> IO<C> {
    return { a1, a2 ->
      val i1 = IO.narrow(a1)
      val i2 = IO.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<IO.T, A>, K1<IO.T, B>, K1<IO.T, C>) -> IO<D> {
    return { a1, a2, a3 ->
      val i1 = IO.narrow(a1)
      val i2 = IO.narrow(a2)
      val i3 = IO.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
}
