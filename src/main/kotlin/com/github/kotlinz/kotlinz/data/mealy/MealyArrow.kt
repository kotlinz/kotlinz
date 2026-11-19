package com.github.kotlinz.kotlinz.data.mealy

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.arrow.Arrow

/** Arrow instance for [Mealy]; [arr] lifts a stateless function. */
interface MealyArrow: Arrow<Mealy.T, Unit>, MealyCategory {
  override fun <A, B> arr(f: (A) -> B): Mealy<A, B> = Mealy { a -> Pair(f(a), arr(f)) }

  override fun <A, B, C> first(f: K3<Mealy.T, Unit, A, B>): Mealy<Pair<A, C>, Pair<B, C>> = firstOf(Mealy.narrow(f))
}

internal fun <A, B, C> firstOf(mealy: Mealy<A, B>): Mealy<Pair<A, C>, Pair<B, C>> =
  Mealy { (a, c) -> mealy.step(a).let { (b, next) -> Pair(Pair(b, c), firstOf(next)) } }
