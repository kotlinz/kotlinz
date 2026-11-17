package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

interface Tuple2Applicative<S: Monoid<*, S>>: Applicative<K1<Tuple2.T, S>> {
  val type: Class<S>

  override fun <A> pure(v: A): Tuple2<S, A> = Tuple2(Monoid.mzero(type), v)

  override fun <A, B> ap(f: K1<K1<Tuple2.T, S>, (A) -> B>, v: K1<K1<Tuple2.T, S>, A>): Tuple2<S, B> {
    val tuplef = Tuple2.narrow(f)
    val tuple = Tuple2.narrow(v)
    return Tuple2(Monoid.op(type, tuplef.first, tuple.first), tuplef.second(tuple.second))
  }
}
