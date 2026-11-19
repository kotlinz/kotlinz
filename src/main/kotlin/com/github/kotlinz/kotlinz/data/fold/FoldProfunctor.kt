package com.github.kotlinz.kotlinz.data.fold

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

/** Profunctor instance for [Fold]: contravariant in the inputs, covariant in the result. */
interface FoldProfunctor: Profunctor<Fold.T, Unit> {
  override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Fold.T, Unit, B, C>): Fold<A, D> {
    val fold = Fold.narrow(v)
    return Fold.of(fold.initial, { s, a: A -> fold.step(s, f(a)) }) { s -> g(fold.done(s)) }
  }

  override fun <A, B, C> lmap(f: (A) -> B, v: K3<Fold.T, Unit, B, C>): Fold<A, C> = Fold.narrow(super.lmap(f, v))
  override fun <A, B, C> rmap(g: (B) -> C, v: K3<Fold.T, Unit, A, B>): Fold<A, C> = Fold.narrow(super.rmap(g, v))
}
