package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.category.Compose

/** Composition (semigroupoid) instance for [Function1]. */
interface Function1Compose: Compose<Function1.T, Unit> {
  override fun <A, B, C> compose(f: K3<Function1.T, Unit, B, C>, g: K3<Function1.T, Unit, A, B>): Function1<A, C> {
    val ff = Function1.narrow(f)
    val gf = Function1.narrow(g)
    return Function1 { a -> ff.run(gf.run(a)) }
  }
}
