package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either

/**
 * An applicative that can fail with an error of type `E` and recover from it.
 *
 * Laws:
 * - `handleErrorWith(f, raiseError(e)) == f(e)`
 * - `handleErrorWith(f, pure(a)) == pure(a)`
 * - `attempt(raiseError(e)) == pure(Left(e))`
 * - `attempt(pure(a)) == pure(Right(a))`
 *
 * @see com.github.kotlinz.kotlinz.type.monad.MonadError
 */
interface ApplicativeError<T, E>: Applicative<T> {
  /** Creates a failed computation carrying `e`. */
  fun <A> raiseError(e: E): K1<T, A>
  /** Recovers from a failure of `v` by switching to the computation returned by `f`. */
  fun <A> handleErrorWith(f: (E) -> K1<T, A>, v: K1<T, A>): K1<T, A>

  /** Recovers from a failure of `v` by replacing it with the pure value `f(e)`. */
  fun <A> handleError(f: (E) -> A, v: K1<T, A>): K1<T, A> = handleErrorWith({ e -> pure(f(e)) }, v)

  /** Exposes failure as a value: errors become `Left`, successes become `Right`. */
  fun <A> attempt(v: K1<T, A>): K1<T, Either<E, A>> {
    val right = fmap({ a: A -> Either.Right<E, A>(a) as Either<E, A> }, v)
    return handleErrorWith({ e -> pure(Either.Left(e)) }, right)
  }

  /** Lifts an `Either`, raising `Left` values as errors. */
  fun <A> fromEither(v: Either<E, A>): K1<T, A> = when (v) {
    is Either.Left -> raiseError(v.value)
    is Either.Right -> pure(v.value)
  }
}
