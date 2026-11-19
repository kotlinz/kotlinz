package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [LazyList]; elements are mapped on demand. */
interface LazyListFunctor: Functor<LazyList.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<LazyList.T, A>): LazyList<B> = LazyList.narrow(v).mapLazy(f)
}
