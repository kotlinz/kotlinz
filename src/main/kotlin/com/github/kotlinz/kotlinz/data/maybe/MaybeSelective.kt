package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.MonadSelective

/** Selective instance for [Maybe]: the function is only needed when the first value is `Just(Left)`. */
interface MaybeSelective: MonadSelective<Maybe.T>, MaybeMonad {
  override fun <A, B> select(fab: K1<Maybe.T, Either<A, B>>, fn: K1<Maybe.T, (A) -> B>): Maybe<B> =
    Maybe.narrow(super.select(fab, fn))
}
