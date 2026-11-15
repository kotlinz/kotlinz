package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface ListKApplicative: Applicative<ListK.T>, ListKFunctor {
  override fun <A> pure(v: A): ListK<A> = ListK(listOf(v))

  override fun <A, B> ap(f: K1<ListK.T, (A) -> B>, v: K1<ListK.T, A>): ListK<B> {
    val list = ListK.narrow(v)
    val listf = ListK.narrow(f)
    return ListK(listf.value.flatMap { g -> list.value.map(g) })
  }
}
