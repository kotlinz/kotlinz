package com.github.kotlinz.kotlinz.data.freearrow

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.category.Compose

/** Compose instance for [FreeArrow]. */
interface FreeArrowCompose<P>: Compose<FreeArrow.T, P> {
  override fun <A, B, C> compose(f: K3<FreeArrow.T, P, B, C>, g: K3<FreeArrow.T, P, A, B>): FreeArrow<P, A, C> =
    FreeArrow.Compose(FreeArrow.narrow(f), FreeArrow.narrow(g))
}
