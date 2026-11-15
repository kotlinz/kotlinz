package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface NonEmptyListApplicative: Applicative<NonEmptyList.T>, NonEmptyListFunctor {
  override fun <A> pure(v: A): NonEmptyList<A> = NonEmptyList(v, listOf())

  override fun <A, B> ap(f: K1<NonEmptyList.T, (A) -> B>, v: K1<NonEmptyList.T, A>): NonEmptyList<B> {
    val list = NonEmptyList.narrow(v)
    val listf = NonEmptyList.narrow(f)
    return listf.tail.fold(NonEmptyList.narrow(fmap(listf.head, list))) { acc, g -> acc + NonEmptyList.narrow(fmap(g, list)) }
  }
}
