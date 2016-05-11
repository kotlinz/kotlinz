package data.either

import K1
import type.Functor

interface EitherFunctor<S>: Functor<K1<Either.µ, S>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Either.µ, S>, A>): Either<S, B> {
    val either = Either.narrow(v)
    return when(either) {
      is Either.Left -> Either.Left(either.value)
      is Either.Right -> Either.Right(f(either.value))
    }
  }
}
