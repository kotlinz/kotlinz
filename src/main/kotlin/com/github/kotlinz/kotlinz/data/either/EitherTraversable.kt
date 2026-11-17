package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface EitherTraversable<S>: Traversable<K1<Either.T, S>>, EitherFunctor<S>, EitherFoldable<S> {
  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<K1<Either.T, S>, A>): K1<G, K1<K1<Either.T, S>, B>> {
    val either = Either.narrow(v)
    return when (either) {
      is Either.Left -> applicative.pure(Either.Left<S, B>(either.value))
      is Either.Right -> applicative.fmap({ b: B -> Either.Right<S, B>(b) as K1<K1<Either.T, S>, B> }, f(either.value))
    }
  }
}
