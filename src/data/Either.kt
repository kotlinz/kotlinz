package data

import K1
import K2
import type.Monad

sealed class Either<L, R>: K2<Either.µ, L, R> {
  class µ {}

  class Left<L, R>(val value: L): Either<L, R>()
  class Right<L, R>(val value: R): Either<L, R>()

  companion object {
    fun <L, R> narrow(v: K1<K1<µ, L>, R>) = v as Either<L, R>
    fun <L, S> pure(v: S): Either<L, S> = (object: EitherMonad<L> {}).pure(v)
  }

  infix fun <S> fmap(f: (R) -> S): Either<L, S> = (object: EitherMonad<L> {}).fmap(f, this)
  infix fun <S> ap(f: Either<L, (R) -> S>): Either<L, S> = (object: EitherMonad<L> {}).ap(f, this)
  infix fun <S> bind(f: (R) -> Either<L, S>): Either<L, S> = (object: EitherMonad<L> {}).bind(f, this)

  private interface EitherMonad<S>: Monad<K1<µ, S>> {
    override fun <A> pure(v: A): Either<S, A> = Right(v)

    override fun <A, B> fmap(f: (A) -> B, v: K1<K1<µ, S>, A>): Either<S, B> {
      val either = narrow(v)
      return when(either) {
        is Left -> Left(either.value)
        is Right -> Right(f(either.value))
      }
    }

    override fun <A, B> ap(f: K1<K1<µ, S>, (A) -> B>, v: K1<K1<µ, S>, A>): Either<S, B> {
      val either = narrow(v)
      val eitherf = narrow(f)
      return when(eitherf) {
        is Right -> fmap(eitherf.value, either)
        is Left -> Left(eitherf.value)
      }
    }
    override fun <A, B> bind(f: (A) -> K1<K1<µ, S>, B>, v: K1<K1<µ, S>, A>): Either<S, B> {
      val either = narrow(v)
      return when(either) {
        is Right -> narrow(f(either.value))
        is Left -> Left(either.value)
      }
    }
  }
}