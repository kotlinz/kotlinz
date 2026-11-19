package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Strong

/** Strong instance for [Function1]: applies the function to one component of a pair. */
interface Function1Strong: Strong<Function1.T, Unit>, Function1Profunctor {
  override fun <A, B, C> first(v: K3<Function1.T, Unit, A, B>): Function1<Pair<A, C>, Pair<B, C>> {
    val function = Function1.narrow(v)
    return Function1 { (a, c) -> Pair(function.run(a), c) }
  }

  override fun <A, B, C> second(v: K3<Function1.T, Unit, A, B>): Function1<Pair<C, A>, Pair<C, B>> {
    val function = Function1.narrow(v)
    return Function1 { (c, a) -> Pair(c, function.run(a)) }
  }
}
