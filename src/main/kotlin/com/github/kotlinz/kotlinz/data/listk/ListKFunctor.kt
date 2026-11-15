package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface ListKFunctor: Functor<ListK.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<ListK.T, A>): ListK<B> = ListK(ListK.narrow(v).value.map(f))
}
