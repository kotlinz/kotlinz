package com.github.kotlinz.kotlinz.data.tagged

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

/** Profunctor instance for [Tagged]: only the output is mapped, the input is phantom. */
interface TaggedProfunctor: Profunctor<Tagged.T, Unit> {
  override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Tagged.T, Unit, B, C>): Tagged<A, D> =
    Tagged(g(Tagged.narrow(v).value))

  override fun <A, B, C> lmap(f: (A) -> B, v: K3<Tagged.T, Unit, B, C>): Tagged<A, C> = Tagged.narrow(super.lmap(f, v))
  override fun <A, B, C> rmap(g: (B) -> C, v: K3<Tagged.T, Unit, A, B>): Tagged<A, C> = Tagged.narrow(super.rmap(g, v))
}
