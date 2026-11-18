package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bifunctor

/** Bifunctor instance for [Tuple2]. */
interface Tuple2Bifunctor : Bifunctor<Tuple2.T> {
  override fun <A, B, C, D> bimap(f: (A) -> C, g: (B) -> D, v: K2<Tuple2.T, A, B>): Tuple2<C, D> {
    val tuple = Tuple2.narrow(v)
    return Tuple2(f(tuple.first), g(tuple.second))
  }

  override fun <A, B, C> first(f: (A) -> C, v: K2<Tuple2.T, A, B>): Tuple2<C, B> = Tuple2.narrow(super.first(f, v))
  override fun <A, B, D> second(g: (B) -> D, v: K2<Tuple2.T, A, B>): Tuple2<A, D> = Tuple2.narrow(super.second(g, v))
}
