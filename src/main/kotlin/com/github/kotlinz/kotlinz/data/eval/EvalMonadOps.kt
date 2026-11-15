package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadOps

interface EvalMonadOps: MonadOps<Eval.T>, EvalMonad {
  override fun <A, B> liftM(f: (A) -> B): (K1<Eval.T, A>) -> K1<Eval.T, B> {
    return { m ->
      val i = Eval.narrow(m)
      i bind { x -> Eval.pure(f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A, B) -> C): (K1<Eval.T, A>, K1<Eval.T, B>) -> K1<Eval.T, C> {
    return { m1, m2 ->
      val i1 = Eval.narrow(m1)
      val i2 = Eval.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> Eval.pure(f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<Eval.T, A>, K1<Eval.T, B>, K1<Eval.T, C>) -> K1<Eval.T, D> {
    return { m1, m2, m3 ->
      val i1 = Eval.narrow(m1)
      val i2 = Eval.narrow(m2)
      val i3 = Eval.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> Eval.pure(f(x1, x2, x3)) } } }
    }
  }
}
