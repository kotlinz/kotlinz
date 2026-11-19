package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Knot
import com.github.kotlinz.kotlinz.type.monad.MonadFix

/** MonadFix instance for [Eval]: the knot is tied lazily when the value is first requested. */
interface EvalMonadFix: MonadFix<Eval.T>, EvalMonad {
  override fun <A> mfix(f: (() -> A) -> K1<Eval.T, A>): Eval<A> =
    Eval.later { Knot.fix { x -> Eval.narrow(f(x)).value } }
}
