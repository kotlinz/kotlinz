package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface TryTraversable: Traversable<Try.T>, TryFunctor, TryFoldable {
  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<Try.T, A>): K1<G, K1<Try.T, B>> {
    val t = Try.narrow(v)
    return when (t) {
      is Try.Success -> applicative.fmap({ b: B -> Try.Success(b) as K1<Try.T, B> }, f(t.value))
      is Try.Failure -> applicative.pure(Try.Failure<B>(t.error))
    }
  }
}
