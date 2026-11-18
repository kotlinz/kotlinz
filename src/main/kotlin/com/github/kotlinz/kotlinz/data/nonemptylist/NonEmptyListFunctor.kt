package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [NonEmptyList]. */
interface NonEmptyListFunctor: Functor<NonEmptyList.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<NonEmptyList.T, A>): NonEmptyList<B> {
    val list = NonEmptyList.narrow(v)
    return NonEmptyList(f(list.head), list.tail.map(f))
  }
}
