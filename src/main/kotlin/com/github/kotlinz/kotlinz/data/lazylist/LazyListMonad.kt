package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [LazyList]; [bind] concatenates the produced lists lazily. */
interface LazyListMonad: Monad<LazyList.T>, LazyListApplicative {
  override fun <A> join(v: K1<LazyList.T, K1<LazyList.T, A>>): LazyList<A> = bind({ it }, v)

  override fun <A, B> bind(f: (A) -> K1<LazyList.T, B>, v: K1<LazyList.T, A>): LazyList<B> =
    LazyList.narrow(v).concatMap { a -> LazyList.narrow(f(a)) }
}
