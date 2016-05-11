package com.github.unhappychoice.kotlinz.data.either

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.monad.MonadOps

interface EitherMonadOps<S>: MonadOps<K1<Either.µ, S>>, EitherMonad<S> {
  override fun <A, B> liftM(f: (A) -> B): (K1<K1<Either.µ, S>, A>) -> K1<K1<Either.µ, S>, B> {
    return { m ->
      val i = Either.narrow(m)
      i bind { x -> Either.pure<S, B>(f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A,  B) -> C): (K1<K1<Either.µ, S>, A>, K1<K1<Either.µ, S>, B>) -> K1<K1<Either.µ, S>, C> {
    return { m1, m2 ->
      val i1 = Either.narrow(m1)
      val i2 = Either.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> Either.pure<S, C>(f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<K1<Either.µ, S>, A>, K1<K1<Either.µ, S>, B>, K1<K1<Either.µ, S>, C>) -> K1<K1<Either.µ, S>, D> {
    return { m1, m2, m3 ->
      val i1 = Either.narrow(m1)
      val i2 = Either.narrow(m2)
      val i3 = Either.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> Either.pure<S, D>(f(x1, x2, x3)) } } }
    }
  }
}