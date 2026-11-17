package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad

interface Tuple2Monad<S: Monoid<*, S>>: Monad<K1<Tuple2.T, S>>, Tuple2Applicative<S>, Tuple2Functor<S> {
  override fun <A> join(v: K1<K1<Tuple2.T, S>, K1<K1<Tuple2.T, S>, A>>): Tuple2<S, A> {
    val outer = Tuple2.narrow(v)
    val inner = Tuple2.narrow(outer.second)
    return Tuple2(Monoid.op(type, outer.first, inner.first), inner.second)
  }

  override fun <A, B> bind(f: (A) -> K1<K1<Tuple2.T, S>, B>, v: K1<K1<Tuple2.T, S>, A>): Tuple2<S, B> {
    return Tuple2.narrow(super.bind(f, v))
  }
}
