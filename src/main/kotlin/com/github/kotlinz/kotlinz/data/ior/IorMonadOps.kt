package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Semigroup
import com.github.kotlinz.kotlinz.type.monad.MonadOps

interface IorMonadOps<S: Semigroup<*, S>>: MonadOps<K1<Ior.T, S>>, IorMonad<S> {
  override fun <A, B> liftM(f: (A) -> B): (K1<K1<Ior.T, S>, A>) -> K1<K1<Ior.T, S>, B> {
    return { m -> bind({ x -> pure(f(x)) }, m) }
  }

  override fun <A, B, C> liftM2(f: (A, B) -> C): (K1<K1<Ior.T, S>, A>, K1<K1<Ior.T, S>, B>) -> K1<K1<Ior.T, S>, C> {
    return { m1, m2 -> bind({ x1 -> bind({ x2 -> pure(f(x1, x2)) }, m2) }, m1) }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<K1<Ior.T, S>, A>, K1<K1<Ior.T, S>, B>, K1<K1<Ior.T, S>, C>) -> K1<K1<Ior.T, S>, D> {
    return { m1, m2, m3 -> bind({ x1 -> bind({ x2 -> bind({ x3 -> pure(f(x1, x2, x3)) }, m3) }, m2) }, m1) }
  }
}
