package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface EvalApplicative: Applicative<Eval.T> {
  override fun <A> pure(v: A): Eval<A> = Eval.now(v)

  override fun <A, B> ap(f: K1<Eval.T, (A) -> B>, v: K1<Eval.T, A>): Eval<B> {
    return Eval { Eval.narrow(f).value(Eval.narrow(v).value) }
  }
}
