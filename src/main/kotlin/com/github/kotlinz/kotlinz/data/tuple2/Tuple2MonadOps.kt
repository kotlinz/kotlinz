package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadOps

/** Monadic lifting helpers for [Tuple2]. */
interface Tuple2MonadOps<S: Monoid<*, S>>: MonadOps<K1<Tuple2.T, S>>, Tuple2Monad<S> {
  override fun <A, B> liftM(f: (A) -> B): (K1<K1<Tuple2.T, S>, A>) -> K1<K1<Tuple2.T, S>, B> {
    return { m -> bind({ x -> pure(f(x)) }, m) }
  }

  override fun <A, B, C> liftM2(f: (A, B) -> C): (K1<K1<Tuple2.T, S>, A>, K1<K1<Tuple2.T, S>, B>) -> K1<K1<Tuple2.T, S>, C> {
    return { m1, m2 -> bind({ x1 -> bind({ x2 -> pure(f(x1, x2)) }, m2) }, m1) }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<K1<Tuple2.T, S>, A>, K1<K1<Tuple2.T, S>, B>, K1<K1<Tuple2.T, S>, C>) -> K1<K1<Tuple2.T, S>, D> {
    return { m1, m2, m3 -> bind({ x1 -> bind({ x2 -> bind({ x3 -> pure(f(x1, x2, x3)) }, m3) }, m2) }, m1) }
  }
}
