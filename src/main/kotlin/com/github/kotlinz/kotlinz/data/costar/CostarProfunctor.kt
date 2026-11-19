package com.github.kotlinz.kotlinz.data.costar

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

/** Profunctor instance for [Costar], mapping the input through [functor]. */
interface CostarProfunctor<F>: Profunctor<Costar.T, F> {
  val functor: Functor<F>

  override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Costar.T, F, B, C>): Costar<F, A, D> {
    val costar = Costar.narrow(v)
    return Costar { fa -> g(costar.run(functor.fmap(f, fa))) }
  }

  override fun <A, B, C> lmap(f: (A) -> B, v: K3<Costar.T, F, B, C>): Costar<F, A, C> = Costar.narrow(super.lmap(f, v))
  override fun <A, B, C> rmap(g: (B) -> C, v: K3<Costar.T, F, A, B>): Costar<F, A, C> = Costar.narrow(super.rmap(g, v))
}
