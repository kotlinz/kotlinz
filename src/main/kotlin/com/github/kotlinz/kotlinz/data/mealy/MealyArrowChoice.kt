package com.github.kotlinz.kotlinz.data.mealy

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.arrow.ArrowChoice

/** ArrowChoice instance for [Mealy]; the wrapped machine only advances on the inputs routed to it. */
interface MealyArrowChoice: ArrowChoice<Mealy.T, Unit>, MealyArrow {
  override fun <A, B, C> left(f: K3<Mealy.T, Unit, A, B>): Mealy<Either<A, C>, Either<B, C>> = leftOf(Mealy.narrow(f))
}

private fun <A, B, C> leftOf(mealy: Mealy<A, B>): Mealy<Either<A, C>, Either<B, C>> = Mealy { e ->
  when (e) {
    is Either.Left -> mealy.step(e.value).let { (b, next) -> Pair(Either.Left<B, C>(b), leftOf(next)) }
    is Either.Right -> Pair(Either.Right<B, C>(e.value), leftOf(mealy))
  }
}
