package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface EitherApplicative<S>: Applicative<K1<Either.T, S>> {
  override fun <A> pure(v: A): Either<S, A> = Either.Right(v)

  override fun <A, B> ap(f: K1<K1<Either.T, S>, (A) -> B>, v: K1<K1<Either.T, S>, A>): Either<S, B> {
    val either = Either.narrow(v)
    val eitherf = Either.narrow(f)
    return when(eitherf) {
      is Either.Right -> Either.narrow(fmap(eitherf.value, either))
      is Either.Left -> Either.Left(eitherf.value)
    }
  }
}
