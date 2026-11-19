package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Closed

/** Closed instance for [Function1]: post-composes the function. */
interface Function1Closed: Closed<Function1.T, Unit>, Function1Profunctor {
  override fun <A, B, X> closed(v: K3<Function1.T, Unit, A, B>): Function1<(X) -> A, (X) -> B> {
    val function = Function1.narrow(v)
    return Function1 { g -> { x -> function.run(g(x)) } }
  }
}
