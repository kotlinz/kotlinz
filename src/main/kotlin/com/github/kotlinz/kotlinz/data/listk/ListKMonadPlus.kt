package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadPlus

interface ListKMonadPlus: MonadPlus<ListK.T>, ListKMonad, ListKAlternative {
  override fun <A> mzero(): ListK<A> = empty()
  override fun <A> mplus(m1: K1<ListK.T, A>, m2: K1<ListK.T, A>): ListK<A> = orElse(m1, m2)
}
