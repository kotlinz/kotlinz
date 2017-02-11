package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2

sealed class Either<L, R>: K2<Either.T, L, R> {
  class T

  class Left<L, R>(val value: L): Either<L, R>()
  class Right<L, R>(val value: R): Either<L, R>()

  companion object {
    fun <L, R> narrow(v: K1<K1<T, L>, R>) = v as Either<L, R>

    // Monad
    fun <L, S> pure(v: S): Either<L, S> = monad<L>().pure(v)
    fun <L, S> join(v: K1<K1<T, L>, K1<K1<T, L>, S>>): Either<L, S> = monad<L>().join(v)
    private fun <L> monad() = object: EitherMonad<L> {}

    // ApplicativeOps
    fun <S, A, B> liftA(f: (A) -> B) = applicativeOps<S>().liftA(f)
    fun <S, A, B, C> liftA2(f: (A, B) -> C) = applicativeOps<S>().liftA2(f)
    fun <S, A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps<S>().liftA3(f)
    private fun <S> applicativeOps() = object: EitherApplicativeOps<S> {}

    // MonadOps
    fun <S, A, B> liftM(f: (A) -> B) = monadOps<S>().liftM(f)
    fun <S, A, B, C> liftM2(f: (A,  B) -> C) =  monadOps<S>().liftM2(f)
    fun <S, A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps<S>().liftM3(f)
    private fun <S> monadOps() = object: EitherMonadOps<S> {}
  }

  // Monad
  infix fun <S> fmap(f: (R) -> S): Either<L, S> = monad.fmap(f, this)
  infix fun <S> ap(f: Either<L, (R) -> S>): Either<L, S> = monad.ap(f, this)
  infix fun <S> bind(f: (R) -> Either<L, S>): Either<L, S> = monad.bind(f, this)
  private val monad = object: EitherMonad<L> {}

  override fun equals(other: Any?): Boolean {
    return when {
      this is Left && other is Left<*, *> -> this.value == other.value
      this is Right && other is Right<*, *> -> this.value == other.value
      else -> false
    }
  }
}