package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Eval]. */
interface EvalFunctor: Functor<Eval.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Eval.T, A>): Eval<B> = Eval { f(Eval.narrow(v).value) }
}
