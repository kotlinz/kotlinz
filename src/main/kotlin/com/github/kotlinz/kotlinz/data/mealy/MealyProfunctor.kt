package com.github.kotlinz.kotlinz.data.mealy

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor

/** Profunctor instance for [Mealy]: contravariant in the input, covariant in the output. */
interface MealyProfunctor: Profunctor<Mealy.T, Unit> {
  override fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<Mealy.T, Unit, B, C>): Mealy<A, D> {
    val mealy = Mealy.narrow(v)
    return Mealy { a -> mealy.step(f(a)).let { (c, next) -> Pair(g(c), dimap(f, g, next)) } }
  }

  override fun <A, B, C> lmap(f: (A) -> B, v: K3<Mealy.T, Unit, B, C>): Mealy<A, C> = Mealy.narrow(super.lmap(f, v))
  override fun <A, B, C> rmap(g: (B) -> C, v: K3<Mealy.T, Unit, A, B>): Mealy<A, C> = Mealy.narrow(super.rmap(g, v))
}
