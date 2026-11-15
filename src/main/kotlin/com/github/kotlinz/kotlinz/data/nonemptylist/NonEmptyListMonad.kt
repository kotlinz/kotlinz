package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

interface NonEmptyListMonad: Monad<NonEmptyList.T>, NonEmptyListApplicative, NonEmptyListFunctor {
  override fun <A> join(v: K1<NonEmptyList.T, K1<NonEmptyList.T, A>>): NonEmptyList<A> {
    val list = NonEmptyList.narrow(v)
    return list.tail.fold(NonEmptyList.narrow(list.head)) { acc, m -> acc + NonEmptyList.narrow(m) }
  }

  override fun <A, B> bind(f: (A) -> K1<NonEmptyList.T, B>, v: K1<NonEmptyList.T, A>): NonEmptyList<B> {
    return NonEmptyList.narrow(super.bind(f, v))
  }
}
