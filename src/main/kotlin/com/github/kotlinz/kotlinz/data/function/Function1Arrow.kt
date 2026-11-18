package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.arrow.Arrow

/** Arrow instance for [Function1]. */
interface Function1Arrow: Arrow<Function1.T, Unit>, Function1Category {
  override fun <A, B> arr(f: (A) -> B): Function1<A, B> = Function1(f)

  override fun <A, B, C> first(f: K3<Function1.T, Unit, A, B>): Function1<Pair<A, C>, Pair<B, C>> {
    val function = Function1.narrow(f)
    return Function1 { (a, c) -> Pair(function.run(a), c) }
  }
}
