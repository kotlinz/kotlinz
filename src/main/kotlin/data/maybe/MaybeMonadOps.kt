package data.maybe

import K1
import type.monad.MonadOps

interface MaybeMonadOps: MonadOps<Maybe.µ>, MaybeMonad {
  override fun <A, B> liftM(f: (A) -> B): (K1<Maybe.µ, A>) -> K1<Maybe.µ, B> {
    return { m ->
      val i = Maybe.narrow(m)
      i bind { x -> Maybe.pure(f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A,  B) -> C): (K1<Maybe.µ, A>, K1<Maybe.µ, B>) -> K1<Maybe.µ, C> {
    return { m1, m2 ->
      val i1 = Maybe.narrow(m1)
      val i2 = Maybe.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> Maybe.pure(f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<Maybe.µ, A>, K1<Maybe.µ, B>, K1<Maybe.µ, C>) -> K1<Maybe.µ, D> {
    return { m1, m2, m3 ->
      val i1 = Maybe.narrow(m1)
      val i2 = Maybe.narrow(m2)
      val i3 = Maybe.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> Maybe.pure(f(x1, x2, x3)) } } }
    }
  }
}