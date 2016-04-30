package data.either

import K1
import K2

sealed class Either<L, R>: K2<Either.µ, L, R> {
  class µ {}

  class Left<L, R>(val value: L): Either<L, R>()
  class Right<L, R>(val value: R): Either<L, R>()

  companion object {
    fun <L, R> narrow(v: K1<K1<µ, L>, R>) = v as Either<L, R>
    fun <L, S> pure(v: S): Either<L, S> = (object: EitherMonad<L> {}).pure(v)
  }

  infix fun <S> fmap(f: (R) -> S): Either<L, S> = monad.fmap(f, this)
  infix fun <S> ap(f: Either<L, (R) -> S>): Either<L, S> = monad.ap(f, this)
  infix fun <S> bind(f: (R) -> Either<L, S>): Either<L, S> = monad.bind(f, this)

  private val monad = object: EitherMonad<L> {}
}