package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadPlus

interface ListKMonadPlus: MonadPlus<ListK.T>, ListKMonad {
  override fun <A> mzero(): ListK<A> = ListK(listOf())

  override fun <A> mplus(m1: K1<ListK.T, A>, m2: K1<ListK.T, A>): ListK<A> {
    return ListK(ListK.narrow(m1).value + ListK.narrow(m2).value)
  }
}
