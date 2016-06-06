package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

interface MaybeApplicativeOps: ApplicativeOps<Maybe.T>, MaybeApplicative {
  override fun <A, B> liftA(f: (A) -> B): (K1<Maybe.T, A>) -> Maybe<B> {
    return { a -> Maybe.narrow(a) ap Maybe.pure(f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<Maybe.T, A>, K1<Maybe.T, B>) -> Maybe<C> {
    return { a1, a2 ->
      val i1 = Maybe.narrow(a1)
      val i2 = Maybe.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<Maybe.T, A>, K1<Maybe.T, B>, K1<Maybe.T, C>) -> Maybe<D> {
    return { a1, a2, a3 ->
      val i1 = Maybe.narrow(a1)
      val i2 = Maybe.narrow(a2)
      val i3 = Maybe.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
}