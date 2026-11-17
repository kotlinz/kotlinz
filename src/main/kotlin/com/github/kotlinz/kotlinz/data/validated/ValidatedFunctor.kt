package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface ValidatedFunctor<E>: Functor<K1<Validated.T, E>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Validated.T, E>, A>): Validated<E, B> {
    return when (val validated = Validated.narrow(v)) {
      is Validated.Valid -> Validated.Valid(f(validated.value))
      is Validated.Invalid -> Validated.Invalid(validated.error)
    }
  }
}
