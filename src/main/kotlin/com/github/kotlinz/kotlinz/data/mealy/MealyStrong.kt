package com.github.kotlinz.kotlinz.data.mealy

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Strong

/** Strong instance for [Mealy]; the extra component is passed through unchanged. */
interface MealyStrong: Strong<Mealy.T, Unit>, MealyProfunctor {
  override fun <A, B, C> first(v: K3<Mealy.T, Unit, A, B>): Mealy<Pair<A, C>, Pair<B, C>> = firstOf(Mealy.narrow(v))

  override fun <A, B, C> second(v: K3<Mealy.T, Unit, A, B>): Mealy<Pair<C, A>, Pair<C, B>> = Mealy.narrow(super.second(v))
}
