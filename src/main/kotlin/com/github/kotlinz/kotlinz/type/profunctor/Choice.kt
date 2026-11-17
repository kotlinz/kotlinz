package com.github.kotlinz.kotlinz.type.profunctor

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either

interface Choice<T, F> : Profunctor<T, F> {
  fun <A, B, C> left(v: K3<T, F, A, B>): K3<T, F, Either<A, C>, Either<B, C>>

  fun <A, B, C> right(v: K3<T, F, A, B>): K3<T, F, Either<C, A>, Either<C, B>> =
    dimap({ e: Either<C, A> -> e.mirror() }, { e: Either<B, C> -> e.mirror() }, left<A, B, C>(v))
}

private fun <X, Y> Either<X, Y>.mirror(): Either<Y, X> = when (this) {
  is Either.Left -> Either.Right(value)
  is Either.Right -> Either.Left(value)
}
