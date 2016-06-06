package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

interface EitherMonad<S>: Monad<K1<Either.T, S>>, EitherApplicative<S>, EitherFunctor<S> {
  override fun <A> join(v: K1<K1<Either.T, S>, K1<K1<Either.T, S>, A>>): Either<S, A> {
    val either = Either.narrow(v)
    return when (either) {
      is Either.Right -> Either.narrow(either.value)
      is Either.Left -> Either.Left(either.value)
    }
  }

  override fun <A, B> bind(f: (A) -> K1<K1<Either.T, S>, B>, v: K1<K1<Either.T, S>, A>): Either<S, B> {
    return Either.narrow(super.bind(f, v))
  }
}