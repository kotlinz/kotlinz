package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.MonadRec

interface EvalMonadRec: MonadRec<Eval.T>, EvalMonad {
  override fun <A, B> tailRecM(a: A, f: (A) -> K1<Eval.T, Either<A, B>>): Eval<B> {
    val step = { x: A -> Eval.narrow(f(x)).value }
    return Eval.later {
      val last = generateSequence(step(a)) { e -> (e as? Either.Left)?.let { step(it.value) } }.last()
      (last as Either.Right).value
    }
  }
}
