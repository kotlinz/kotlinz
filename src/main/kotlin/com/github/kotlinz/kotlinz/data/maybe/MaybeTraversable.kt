package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Traversable instance for [Maybe]. */
interface MaybeTraversable: Traversable<Maybe.T>, MaybeFunctor, MaybeFoldable {
  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<Maybe.T, A>): K1<G, K1<Maybe.T, B>> {
    val maybe = Maybe.narrow(v)
    return when (maybe) {
      is Maybe.Just -> applicative.fmap({ b: B -> Maybe.Just(b) as K1<Maybe.T, B> }, f(maybe.value))
      is Maybe.None -> applicative.pure(Maybe.None<B>())
    }
  }
}
