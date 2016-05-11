package data.maybe

import K1
import curried
import type.applicative.ApplicativeOps

interface MaybeApplicativeOps: ApplicativeOps<Maybe.µ>, MaybeApplicative {
  override fun <A, B> liftA(f: (A) -> B): (K1<Maybe.µ, A>) -> Maybe<B> {
    return { a -> Maybe.narrow(a) ap Maybe.pure(f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<Maybe.µ, A>, K1<Maybe.µ, B>) -> Maybe<C> {
    return { a1, a2 ->
      val i1 = Maybe.narrow(a1)
      val i2 = Maybe.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<Maybe.µ, A>, K1<Maybe.µ, B>, K1<Maybe.µ, C>) -> Maybe<D> {
    return { a1, a2, a3 ->
      val i1 = Maybe.narrow(a1)
      val i2 = Maybe.narrow(a2)
      val i3 = Maybe.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
}