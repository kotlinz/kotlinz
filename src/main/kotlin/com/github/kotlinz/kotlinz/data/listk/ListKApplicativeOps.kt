package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

/** Applicative lifting helpers for [ListK]. */
interface ListKApplicativeOps: ApplicativeOps<ListK.T>, ListKApplicative {
  override fun <A, B> liftA(f: (A) -> B): (K1<ListK.T, A>) -> ListK<B> {
    return { a -> ListK.narrow(a) ap ListK.pure(f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<ListK.T, A>, K1<ListK.T, B>) -> ListK<C> {
    return { a1, a2 ->
      val i1 = ListK.narrow(a1)
      val i2 = ListK.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<ListK.T, A>, K1<ListK.T, B>, K1<ListK.T, C>) -> ListK<D> {
    return { a1, a2, a3 ->
      val i1 = ListK.narrow(a1)
      val i2 = ListK.narrow(a2)
      val i3 = ListK.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
}