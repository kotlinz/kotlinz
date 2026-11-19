package com.github.kotlinz.kotlinz.data.mealy

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Mealy] with a fixed input type [I]; maps every output. */
interface MealyFunctor<I>: Functor<K2<Mealy.T, Unit, I>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K2<Mealy.T, Unit, I>, A>): Mealy<I, B> {
    val mealy = Mealy.narrow(v)
    return Mealy { i -> mealy.step(i).let { (a, next) -> Pair(f(a), fmap(f, next)) } }
  }
}
