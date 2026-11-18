package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.MonadOps

/** Derived monadic helpers (liftM..liftM3) for [Function1]. */
interface Function1MonadOps<R>: MonadOps<K2<Function1.T, Unit, R>>, Function1Monad<R> {
  override fun <A, B> liftM(f: (A) -> B): (K1<K2<Function1.T, Unit, R>, A>) -> K1<K2<Function1.T, Unit, R>, B> {
    return { m -> bind({ x -> pure(f(x)) }, m) }
  }

  override fun <A, B, C> liftM2(f: (A, B) -> C): (K1<K2<Function1.T, Unit, R>, A>, K1<K2<Function1.T, Unit, R>, B>) -> K1<K2<Function1.T, Unit, R>, C> {
    return { m1, m2 -> bind({ x1 -> bind({ x2 -> pure(f(x1, x2)) }, m2) }, m1) }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<K2<Function1.T, Unit, R>, A>, K1<K2<Function1.T, Unit, R>, B>, K1<K2<Function1.T, Unit, R>, C>) -> K1<K2<Function1.T, Unit, R>, D> {
    return { m1, m2, m3 -> bind({ x1 -> bind({ x2 -> bind({ x3 -> pure(f(x1, x2, x3)) }, m3) }, m2) }, m1) }
  }
}
