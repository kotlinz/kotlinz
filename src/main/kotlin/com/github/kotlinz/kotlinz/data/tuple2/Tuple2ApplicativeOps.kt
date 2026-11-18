package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps
import com.github.kotlinz.kotlinz.type.group.Monoid

/** Applicative lifting helpers for [Tuple2]. */
interface Tuple2ApplicativeOps<S: Monoid<*, S>>: ApplicativeOps<K1<Tuple2.T, S>>, Tuple2Monad<S> {
  override fun <A, B> liftA(f: (A) -> B): (K1<K1<Tuple2.T, S>, A>) -> Tuple2<S, B> {
    return { a -> ap(pure(f), a) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<K1<Tuple2.T, S>, A>, K1<K1<Tuple2.T, S>, B>) -> Tuple2<S, C> {
    return { a1, a2 -> ap(fmap(f.curried(), a1), a2) }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<K1<Tuple2.T, S>, A>, K1<K1<Tuple2.T, S>, B>, K1<K1<Tuple2.T, S>, C>) -> Tuple2<S, D> {
    return { a1, a2, a3 -> ap(ap(fmap(f.curried(), a1), a2), a3) }
  }
}
