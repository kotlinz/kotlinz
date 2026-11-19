package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.profunctor.Choice

/** Choice instance for [Function1]: applies the function to one branch of an `Either`. */
interface Function1Choice: Choice<Function1.T, Unit>, Function1Profunctor {
  override fun <A, B, C> left(v: K3<Function1.T, Unit, A, B>): Function1<Either<A, C>, Either<B, C>> {
    val function = Function1.narrow(v)
    return Function1 { e ->
      when (e) {
        is Either.Left -> Either.Left(function.run(e.value))
        is Either.Right -> Either.Right(e.value)
      }
    }
  }

  override fun <A, B, C> right(v: K3<Function1.T, Unit, A, B>): Function1<Either<C, A>, Either<C, B>> {
    val function = Function1.narrow(v)
    return Function1 { e ->
      when (e) {
        is Either.Left -> Either.Left(e.value)
        is Either.Right -> Either.Right(function.run(e.value))
      }
    }
  }
}
