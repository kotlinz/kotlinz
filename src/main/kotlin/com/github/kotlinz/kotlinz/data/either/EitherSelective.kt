package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadSelective

/** Selective instance for [Either]: a [Either.Left] first value short-circuits, so the function is never inspected. */
interface EitherSelective<S>: MonadSelective<K1<Either.T, S>>, EitherMonad<S> {
  override fun <A, B> select(fab: K1<K1<Either.T, S>, Either<A, B>>, fn: K1<K1<Either.T, S>, (A) -> B>): Either<S, B> =
    Either.narrow(super.select(fab, fn))
}
