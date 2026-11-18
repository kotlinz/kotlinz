package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [ListK]. */
interface ListKMonad: Monad<ListK.T>, ListKApplicative, ListKFunctor {
  override fun <A> join(v: K1<ListK.T, K1<ListK.T, A>>): ListK<A> {
    return ListK(ListK.narrow(v).value.flatMap { ListK.narrow(it).value })
  }

  override fun <A, B> bind(f: (A) -> K1<ListK.T, B>, v: K1<ListK.T, A>): ListK<B> {
    return ListK.narrow(super.bind(f, v))
  }
}
