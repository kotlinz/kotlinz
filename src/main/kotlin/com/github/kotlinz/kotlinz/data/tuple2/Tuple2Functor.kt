package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface Tuple2Functor<S>: Functor<K1<Tuple2.T, S>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Tuple2.T, S>, A>): Tuple2<S, B> {
    val tuple = Tuple2.narrow(v)
    return Tuple2(tuple.first, f(tuple.second))
  }
}
