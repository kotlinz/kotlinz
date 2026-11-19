package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bitraversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Bitraversable instance for [Either]. */
interface EitherBitraversable : Bitraversable<Either.T>, EitherBifunctor, EitherBifoldable {
  override fun <G, A, B, C, D> bitraverse(applicative: Applicative<G>, f: (A) -> K1<G, C>, g: (B) -> K1<G, D>, v: K2<Either.T, A, B>): K1<G, K2<Either.T, C, D>> {
    return when (val either = Either.narrow(v)) {
      is Either.Left -> applicative.fmap({ c: C -> Either.Left<C, D>(c) }, f(either.value))
      is Either.Right -> applicative.fmap({ d: D -> Either.Right<C, D>(d) }, g(either.value))
    }
  }
}
