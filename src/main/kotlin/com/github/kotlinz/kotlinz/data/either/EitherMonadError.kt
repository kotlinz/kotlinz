package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadError

/** MonadError instance for [Either] with the left type as the error type. */
interface EitherMonadError<L>: MonadError<K1<Either.T, L>, L>, EitherMonad<L> {
  override fun <A> raiseError(e: L): Either<L, A> = Either.Left(e)

  override fun <A> handleErrorWith(f: (L) -> K1<K1<Either.T, L>, A>, v: K1<K1<Either.T, L>, A>): Either<L, A> {
    val either = Either.narrow(v)
    return when (either) {
      is Either.Left -> Either.narrow(f(either.value))
      is Either.Right -> either
    }
  }
}
