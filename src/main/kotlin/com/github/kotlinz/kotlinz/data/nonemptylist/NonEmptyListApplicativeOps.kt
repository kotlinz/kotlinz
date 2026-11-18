package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

/** ApplicativeOps (liftA family) for [NonEmptyList]. */
interface NonEmptyListApplicativeOps: ApplicativeOps<NonEmptyList.T>, NonEmptyListApplicative {
  override fun <A, B> liftA(f: (A) -> B): (K1<NonEmptyList.T, A>) -> NonEmptyList<B> {
    return { a -> NonEmptyList.narrow(a) ap NonEmptyList.pure(f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<NonEmptyList.T, A>, K1<NonEmptyList.T, B>) -> NonEmptyList<C> {
    return { a1, a2 ->
      val i1 = NonEmptyList.narrow(a1)
      val i2 = NonEmptyList.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<NonEmptyList.T, A>, K1<NonEmptyList.T, B>, K1<NonEmptyList.T, C>) -> NonEmptyList<D> {
    return { a1, a2, a3 ->
      val i1 = NonEmptyList.narrow(a1)
      val i2 = NonEmptyList.narrow(a2)
      val i3 = NonEmptyList.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
}