package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Comonad

interface Tuple2Comonad<S>: Comonad<K1<Tuple2.T, S>>, Tuple2Copointed<S>, Tuple2Functor<S> {
  override fun <A> duplicate(v: K1<K1<Tuple2.T, S>, A>): Tuple2<S, K1<K1<Tuple2.T, S>, A>> {
    val tuple = Tuple2.narrow(v)
    return Tuple2(tuple.first, tuple)
  }

  override fun <A, B> extend(f: (K1<K1<Tuple2.T, S>, A>) -> B, v: K1<K1<Tuple2.T, S>, A>): Tuple2<S, B> {
    return fmap(f, duplicate(v))
  }
}
