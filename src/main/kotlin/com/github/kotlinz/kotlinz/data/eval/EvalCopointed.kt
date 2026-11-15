package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.pointed.Copointed

interface EvalCopointed: Copointed<Eval.T> {
  override fun <A> extract(v: K1<Eval.T, A>): A = Eval.narrow(v).value
}
