package com.github.kotlinz.kotlinz.data.cayley

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

/** Profunctor instance for [Cayley]: maps every wrapped profunctor value. */
interface CayleyProfunctor<F, PT, PF>: Profunctor<Cayley.T, Cayley.Of<F, PT, PF>> {
  /** The functor wrapping the profunctor values. */
  val functor: Functor<F>
  /** The profunctor being wrapped. */
  val profunctor: Profunctor<PT, PF>

  override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Cayley.T, Cayley.Of<F, PT, PF>, B, C>): Cayley<F, PT, PF, A, D> =
    Cayley(functor.fmap({ pbc: K3<PT, PF, B, C> -> profunctor.dimap(f, g, pbc) }, Cayley.narrow(v).value))

  override fun <A, B, C> lmap(f: (A) -> B, v: K3<Cayley.T, Cayley.Of<F, PT, PF>, B, C>): Cayley<F, PT, PF, A, C> = Cayley.narrow(super.lmap(f, v))
  override fun <A, B, C> rmap(g: (B) -> C, v: K3<Cayley.T, Cayley.Of<F, PT, PF>, A, B>): Cayley<F, PT, PF, A, C> = Cayley.narrow(super.rmap(g, v))
}
