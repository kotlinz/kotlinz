package com.github.kotlinz.kotlinz.data.tagged

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Closed

/** Closed instance for [Tagged]: the carried value becomes a constant function. */
interface TaggedClosed: Closed<Tagged.T, Unit>, TaggedProfunctor {
  override fun <A, B, X> closed(v: K3<Tagged.T, Unit, A, B>): Tagged<(X) -> A, (X) -> B> {
    val value = Tagged.narrow(v).value
    return Tagged { _: X -> value }
  }
}
