package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.Filterable

/** Filterable instance for [ListK], preserving the order of the kept elements. */
interface ListKFilterable: Filterable<ListK.T>, ListKFunctor {
  override fun <A, B> mapFilter(f: (A) -> Maybe<B>, v: K1<ListK.T, A>): ListK<B> =
    ListK(ListK.narrow(v).value.flatMap { a -> justToList(f(a)) })

  override fun <A> filter(p: (A) -> Boolean, v: K1<ListK.T, A>): ListK<A> = ListK(ListK.narrow(v).value.filter(p))
  override fun <A, B: Any> collect(f: (A) -> B?, v: K1<ListK.T, A>): ListK<B> = ListK(ListK.narrow(v).value.mapNotNull(f))
  override fun <A> catMaybes(v: K1<ListK.T, Maybe<A>>): ListK<A> = ListK.narrow(super.catMaybes(v))
}

private fun <B> justToList(m: Maybe<B>): List<B> = if (m is Maybe.Just) listOf(m.value) else listOf()
