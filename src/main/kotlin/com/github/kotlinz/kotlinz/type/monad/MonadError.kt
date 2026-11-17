package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeError

interface MonadError<T, E>: Monad<T>, ApplicativeError<T, E> {
  fun <A> ensure(error: (A) -> E, predicate: (A) -> Boolean, v: K1<T, A>): K1<T, A> {
    return bind({ a -> if (predicate(a)) pure(a) else raiseError(error(a)) }, v)
  }

  fun <A> rethrow(v: K1<T, Either<E, A>>): K1<T, A> = bind({ either -> fromEither(either) }, v)
}
