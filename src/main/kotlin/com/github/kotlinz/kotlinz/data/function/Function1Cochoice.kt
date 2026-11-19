package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.profunctor.Cochoice

/**
 * Cochoice instance for [Function1]: feeds the carried branch back into the function until it
 * produces the wanted branch (diverges if it never does).
 */
interface Function1Cochoice: Cochoice<Function1.T, Unit>, Function1Profunctor {
  override fun <A, B, C> unleft(v: K3<Function1.T, Unit, Either<A, C>, Either<B, C>>): Function1<A, B> {
    val function = Function1.narrow(v)
    return Function1 { a -> loop(function.run, Either.Left(a)) }
  }
}

private tailrec fun <A, B, C> loop(f: (Either<A, C>) -> Either<B, C>, input: Either<A, C>): B =
  when (val output = f(input)) {
    is Either.Left -> output.value
    is Either.Right -> loop(f, Either.Right(output.value))
  }
