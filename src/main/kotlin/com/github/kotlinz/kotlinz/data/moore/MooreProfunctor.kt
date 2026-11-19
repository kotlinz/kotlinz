package com.github.kotlinz.kotlinz.data.moore

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

/** Profunctor instance for [Moore]: contravariant in the input, covariant in the output. */
interface MooreProfunctor: Profunctor<Moore.T, Unit> {
  override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Moore.T, Unit, B, C>): Moore<A, D> {
    val moore = Moore.narrow(v)
    return Moore(g(moore.output)) { a -> dimap(f, g, moore.step(f(a))) }
  }

  override fun <A, B, C> lmap(f: (A) -> B, v: K3<Moore.T, Unit, B, C>): Moore<A, C> = Moore.narrow(super.lmap(f, v))
  override fun <A, B, C> rmap(g: (B) -> C, v: K3<Moore.T, Unit, A, B>): Moore<A, C> = Moore.narrow(super.rmap(g, v))
}
