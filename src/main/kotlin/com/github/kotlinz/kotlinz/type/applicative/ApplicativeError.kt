package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either

interface ApplicativeError<T, E>: Applicative<T> {
  fun <A> raiseError(e: E): K1<T, A>
  fun <A> handleErrorWith(f: (E) -> K1<T, A>, v: K1<T, A>): K1<T, A>

  fun <A> handleError(f: (E) -> A, v: K1<T, A>): K1<T, A> = handleErrorWith({ e -> pure(f(e)) }, v)

  fun <A> attempt(v: K1<T, A>): K1<T, Either<E, A>> {
    val right = fmap({ a: A -> Either.Right<E, A>(a) as Either<E, A> }, v)
    return handleErrorWith({ e -> pure(Either.Left(e)) }, right)
  }

  fun <A> fromEither(v: Either<E, A>): K1<T, A> = when (v) {
    is Either.Left -> raiseError(v.value)
    is Either.Right -> pure(v.value)
  }
}
