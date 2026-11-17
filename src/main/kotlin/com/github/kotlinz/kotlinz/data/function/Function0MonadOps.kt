package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadOps

interface Function0MonadOps: MonadOps<Function0.T>, Function0Monad {
  override fun <A, B> liftM(f: (A) -> B): (K1<Function0.T, A>) -> K1<Function0.T, B> {
    return { m -> bind({ x -> pure(f(x)) }, m) }
  }

  override fun <A, B, C> liftM2(f: (A, B) -> C): (K1<Function0.T, A>, K1<Function0.T, B>) -> K1<Function0.T, C> {
    return { m1, m2 -> bind({ x1 -> bind({ x2 -> pure(f(x1, x2)) }, m2) }, m1) }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<Function0.T, A>, K1<Function0.T, B>, K1<Function0.T, C>) -> K1<Function0.T, D> {
    return { m1, m2, m3 -> bind({ x1 -> bind({ x2 -> bind({ x3 -> pure(f(x1, x2, x3)) }, m3) }, m2) }, m1) }
  }
}
