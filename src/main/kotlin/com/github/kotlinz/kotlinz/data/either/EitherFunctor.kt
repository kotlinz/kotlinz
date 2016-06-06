package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface EitherFunctor<S>: Functor<K1<Either.T, S>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Either.T, S>, A>): Either<S, B> {
    val either = Either.narrow(v)
    return when(either) {
      is Either.Left -> Either.Left(either.value)
      is Either.Right -> Either.Right(f(either.value))
    }
  }
}
