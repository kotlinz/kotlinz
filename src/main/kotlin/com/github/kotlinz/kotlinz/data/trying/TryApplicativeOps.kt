package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

/** Applicative lifting helpers for [Try]. */
interface TryApplicativeOps: ApplicativeOps<Try.T>, TryApplicative {
  override fun <A, B> liftA(f: (A) -> B): (K1<Try.T, A>) -> Try<B> {
    return { a -> Try.narrow(a) ap Try.pure(f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<Try.T, A>, K1<Try.T, B>) -> Try<C> {
    return { a1, a2 ->
      val i1 = Try.narrow(a1)
      val i2 = Try.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<Try.T, A>, K1<Try.T, B>, K1<Try.T, C>) -> Try<D> {
    return { a1, a2, a3 ->
      val i1 = Try.narrow(a1)
      val i2 = Try.narrow(a2)
      val i3 = Try.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
}