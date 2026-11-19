package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bitraversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Bitraversable instance for [Validated]; traverses the error with the first function and the value with the second. */
interface ValidatedBitraversable : Bitraversable<Validated.T>, ValidatedBifunctor, ValidatedBifoldable {
  override fun <G, A, B, C, D> bitraverse(applicative: Applicative<G>, f: (A) -> K1<G, C>, g: (B) -> K1<G, D>, v: K2<Validated.T, A, B>): K1<G, K2<Validated.T, C, D>> {
    return when (val validated = Validated.narrow(v)) {
      is Validated.Invalid -> applicative.fmap({ c: C -> Validated.invalid<C, D>(c) }, f(validated.error))
      is Validated.Valid -> applicative.fmap({ d: D -> Validated.valid<C, D>(d) }, g(validated.value))
    }
  }
}
