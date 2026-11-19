package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [LazyList] with cartesian-product semantics, evaluated lazily. */
interface LazyListApplicative: Applicative<LazyList.T>, LazyListFunctor {
  override fun <A> pure(v: A): LazyList<A> = LazyList.cons(v) { LazyList.empty() }

  override fun <A, B> ap(f: K1<LazyList.T, (A) -> B>, v: K1<LazyList.T, A>): LazyList<B> =
    LazyList.narrow(f).concatMap { g -> LazyList.narrow(v).mapLazy(g) }
}
