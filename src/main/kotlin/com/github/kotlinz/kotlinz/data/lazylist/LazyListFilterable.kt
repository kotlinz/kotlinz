package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.Filterable

/** Filterable instance for [LazyList]; elements are filtered on demand, so it works on infinite lists. */
interface LazyListFilterable: Filterable<LazyList.T>, LazyListFunctor {
  override fun <A, B> mapFilter(f: (A) -> Maybe<B>, v: K1<LazyList.T, A>): LazyList<B> =
    LazyList.narrow(v).concatMap { a -> justToLazyList(f(a)) }

  override fun <A> filter(p: (A) -> Boolean, v: K1<LazyList.T, A>): LazyList<A> = LazyList.narrow(super.filter(p, v))
  override fun <A, B: Any> collect(f: (A) -> B?, v: K1<LazyList.T, A>): LazyList<B> = LazyList.narrow(super.collect(f, v))
  override fun <A> catMaybes(v: K1<LazyList.T, Maybe<A>>): LazyList<A> = LazyList.narrow(super.catMaybes(v))
}

private fun <B> justToLazyList(m: Maybe<B>): LazyList<B> = if (m is Maybe.Just) LazyList.pure(m.value) else LazyList.empty()
