package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bifunctor

/** Bifunctor instance for [Either]. */
interface EitherBifunctor : Bifunctor<Either.T> {
  override fun <A, B, C, D> bimap(f: (A) -> C, g: (B) -> D, v: K2<Either.T, A, B>): Either<C, D> {
    return when (val either = Either.narrow(v)) {
      is Either.Left -> Either.Left(f(either.value))
      is Either.Right -> Either.Right(g(either.value))
    }
  }

  override fun <A, B, C> first(f: (A) -> C, v: K2<Either.T, A, B>): Either<C, B> = Either.narrow(super.first(f, v))
  override fun <A, B, D> second(g: (B) -> D, v: K2<Either.T, A, B>): Either<A, D> = Either.narrow(super.second(g, v))
}
