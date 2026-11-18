package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Eval]; long [bind] chains are forced recursively and are not stack-safe. */
interface EvalMonad: Monad<Eval.T>, EvalApplicative, EvalFunctor {
  override fun <A> join(v: K1<Eval.T, K1<Eval.T, A>>): Eval<A> {
    return Eval { Eval.narrow(Eval.narrow(v).value).value }
  }

  override fun <A, B> bind(f: (A) -> K1<Eval.T, B>, v: K1<Eval.T, A>): Eval<B> {
    return Eval.narrow(super.bind(f, v))
  }
}
