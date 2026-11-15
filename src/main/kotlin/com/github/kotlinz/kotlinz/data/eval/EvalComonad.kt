package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Comonad

interface EvalComonad: Comonad<Eval.T>, EvalCopointed, EvalFunctor {
  override fun <A> duplicate(v: K1<Eval.T, A>): Eval<K1<Eval.T, A>> = Eval.now(v)

  override fun <A, B> extend(f: (K1<Eval.T, A>) -> B, v: K1<Eval.T, A>): Eval<B> {
    return Eval.narrow(fmap(f, duplicate(v)))
  }
}
