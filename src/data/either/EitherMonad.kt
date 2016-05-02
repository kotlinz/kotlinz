package data.either

import K1
import type.monad.Monad

interface EitherMonad<S>: Monad<K1<Either.µ, S>> {
  override fun <A> pure(v: A): Either<S, A> = Either.Right(v)

  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Either.µ, S>, A>): Either<S, B> {
    val either = Either.narrow(v)
    return when(either) {
      is Either.Left -> Either.Left(either.value)
      is Either.Right -> Either.Right(f(either.value))
    }
  }

  override fun <A, B> ap(f: K1<K1<Either.µ, S>, (A) -> B>, v: K1<K1<Either.µ, S>, A>): Either<S, B> {
    val either = Either.narrow(v)
    val eitherf = Either.narrow(f)
    return when(eitherf) {
      is Either.Right -> fmap(eitherf.value, either)
      is Either.Left -> Either.Left(eitherf.value)
    }
  }

  override fun <A> join(v: K1<K1<Either.µ, S>, K1<K1<Either.µ, S>, A>>): Either<S, A> {
    val either = Either.narrow(v)
    return when (either) {
      is Either.Right -> Either.narrow(either.value)
      is Either.Left -> Either.Left(either.value)
    }
  }

  override fun <A, B> bind(f: (A) -> K1<K1<Either.µ, S>, B>, v: K1<K1<Either.µ, S>, A>): Either<S, B> {
    return Either.narrow(super.bind(f, v))
  }
}