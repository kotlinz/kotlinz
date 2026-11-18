package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadZip

/** MonadZip instance for [ListK], zipping element-wise and truncating to the shorter list. */
interface ListKMonadZip: MonadZip<ListK.T>, ListKMonad {
  override fun <A, B> mzip(m1: K1<ListK.T, A>, m2: K1<ListK.T, B>): ListK<Pair<A, B>> {
    return ListK(ListK.narrow(m1).value.zip(ListK.narrow(m2).value))
  }

  override fun <A, B, C> mzipWith(m1: K1<ListK.T, A>, m2: K1<ListK.T, B>, f: (A, B) -> C): ListK<C> {
    return ListK(ListK.narrow(m1).value.zip(ListK.narrow(m2).value, f))
  }

  override fun <A, B> munzip(m: K1<ListK.T, Pair<A, B>>): Pair<ListK<A>, ListK<B>> {
    val (first, second) = ListK.narrow(m).value.unzip()
    return Pair(ListK(first), ListK(second))
  }
}
