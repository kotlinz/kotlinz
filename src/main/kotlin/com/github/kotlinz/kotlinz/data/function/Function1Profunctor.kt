package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

interface Function1Profunctor: Profunctor<Function1.T, Unit> {
  override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Function1.T, Unit, B, C>): Function1<A, D> {
    val function = Function1.narrow(v)
    return Function1 { a -> g(function.run(f(a))) }
  }

  override fun <A, B, C> lmap(f: (A) -> B, v: K3<Function1.T, Unit, B, C>): Function1<A, C> = Function1.narrow(super.lmap(f, v))
  override fun <A, B, C> rmap(g: (B) -> C, v: K3<Function1.T, Unit, A, B>): Function1<A, C> = Function1.narrow(super.rmap(g, v))
}
