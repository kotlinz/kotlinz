package com.github.kotlinz.kotlinz.data.mealy

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.category.Compose

/** Compose instance for [Mealy]: the outputs of `g` are fed into `f`, both advancing in lockstep. */
interface MealyCompose: Compose<Mealy.T, Unit> {
  override fun <A, B, C> compose(f: K3<Mealy.T, Unit, B, C>, g: K3<Mealy.T, Unit, A, B>): Mealy<A, C> {
    val mf = Mealy.narrow(f)
    val mg = Mealy.narrow(g)
    return Mealy { a ->
      val (b, nextG) = mg.step(a)
      val (c, nextF) = mf.step(b)
      Pair(c, compose(nextF, nextG))
    }
  }
}
