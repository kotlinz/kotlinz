package com.github.kotlinz.kotlinz.data.tagged

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.profunctor.Choice

/** Choice instance for [Tagged]: the carried value is injected into the selected branch. */
interface TaggedChoice: Choice<Tagged.T, Unit>, TaggedProfunctor {
  override fun <A, B, C> left(v: K3<Tagged.T, Unit, A, B>): Tagged<Either<A, C>, Either<B, C>> =
    Tagged(Either.Left(Tagged.narrow(v).value))

  override fun <A, B, C> right(v: K3<Tagged.T, Unit, A, B>): Tagged<Either<C, A>, Either<C, B>> =
    Tagged(Either.Right(Tagged.narrow(v).value))
}
