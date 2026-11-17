package com.github.kotlinz.kotlinz.type.transformation

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.maybe.Maybe

class EitherToMaybe<E>: NaturalTransformation<K1<Either.T, E>, Maybe.T> {
  override fun <A> invoke(fa: K1<K1<Either.T, E>, A>): Maybe<A> = when (val either = Either.narrow(fa)) {
    is Either.Right -> Maybe.Just(either.value)
    is Either.Left -> Maybe.None()
  }
}
