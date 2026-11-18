package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeError

/**
 * A monad that can fail with an error of type `E` and recover from it.
 *
 * Laws (in addition to the ApplicativeError laws):
 * - Left zero: `bind(f, raiseError(e)) == raiseError(e)`
 * - `ensure(err, p, pure(a)) == if (p(a)) pure(a) else raiseError(err(a))`
 *
 * @see com.github.kotlinz.kotlinz.type.applicative.ApplicativeError
 */
interface MonadError<T, E>: Monad<T>, ApplicativeError<T, E> {
  /** Fails with `error(a)` when the successful result `a` does not satisfy `predicate`. */
  fun <A> ensure(error: (A) -> E, predicate: (A) -> Boolean, v: K1<T, A>): K1<T, A> {
    return bind({ a -> if (predicate(a)) pure(a) else raiseError(error(a)) }, v)
  }

  /** Inverse of [attempt]: raises `Left` values as errors. */
  fun <A> rethrow(v: K1<T, Either<E, A>>): K1<T, A> = bind({ either -> fromEither(either) }, v)
}
