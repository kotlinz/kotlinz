package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Comonad

interface NonEmptyListComonad: Comonad<NonEmptyList.T>, NonEmptyListCopointed, NonEmptyListFunctor {
  override fun <A> duplicate(v: K1<NonEmptyList.T, A>): NonEmptyList<K1<NonEmptyList.T, A>> {
    val list = NonEmptyList.narrow(v)
    val suffixes = list.tail.indices.map { NonEmptyList(list.tail[it], list.tail.drop(it + 1)) }
    return NonEmptyList(list, suffixes)
  }

  override fun <A, B> extend(f: (K1<NonEmptyList.T, A>) -> B, v: K1<NonEmptyList.T, A>): NonEmptyList<B> {
    return NonEmptyList.narrow(fmap(f, duplicate(v)))
  }
}
