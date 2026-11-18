package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

/** Applicative lifting helpers for [Eval]. */
interface EvalApplicativeOps: ApplicativeOps<Eval.T>, EvalMonad {
  override fun <A, B> liftA(f: (A) -> B): (K1<Eval.T, A>) -> Eval<B> {
    return { a -> Eval.narrow(a) ap Eval.pure(f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<Eval.T, A>, K1<Eval.T, B>) -> Eval<C> {
    return { a1, a2 ->
      val i1 = Eval.narrow(a1)
      val i2 = Eval.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<Eval.T, A>, K1<Eval.T, B>, K1<Eval.T, C>) -> Eval<D> {
    return { a1, a2, a3 ->
      val i1 = Eval.narrow(a1)
      val i2 = Eval.narrow(a2)
      val i3 = Eval.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
}
