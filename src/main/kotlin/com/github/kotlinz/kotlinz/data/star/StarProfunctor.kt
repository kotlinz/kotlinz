package com.github.kotlinz.kotlinz.data.star

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

/** Profunctor instance for [Star], mapping the output through [functor]. */
interface StarProfunctor<F>: Profunctor<Star.T, F> {
  val functor: Functor<F>

  override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Star.T, F, B, C>): Star<F, A, D> {
    val star = Star.narrow(v)
    return Star { a -> functor.fmap(g, star.run(f(a))) }
  }

  override fun <A, B, C> lmap(f: (A) -> B, v: K3<Star.T, F, B, C>): Star<F, A, C> = Star.narrow(super.lmap(f, v))
  override fun <A, B, C> rmap(g: (B) -> C, v: K3<Star.T, F, A, B>): Star<F, A, C> = Star.narrow(super.rmap(g, v))
}
