package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Filterable

/** Filterable instance for [Maybe]; a dropped value becomes [Maybe.None]. */
interface MaybeFilterable: Filterable<Maybe.T>, MaybeFunctor {
  override fun <A, B> mapFilter(f: (A) -> Maybe<B>, v: K1<Maybe.T, A>): Maybe<B> {
    return when (val maybe = Maybe.narrow(v)) {
      is Maybe.Just -> f(maybe.value)
      is Maybe.None -> Maybe.None()
    }
  }

  override fun <A> filter(p: (A) -> Boolean, v: K1<Maybe.T, A>): Maybe<A> = Maybe.narrow(super.filter(p, v))
  override fun <A, B: Any> collect(f: (A) -> B?, v: K1<Maybe.T, A>): Maybe<B> = Maybe.narrow(super.collect(f, v))
  override fun <A> catMaybes(v: K1<Maybe.T, Maybe<A>>): Maybe<A> = Maybe.narrow(super.catMaybes(v))
}
