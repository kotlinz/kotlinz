package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bifunctor

/** Bifunctor instance for [Validated]; the error is the first argument and the valid value the second. */
interface ValidatedBifunctor : Bifunctor<Validated.T> {
  override fun <A, B, C, D> bimap(f: (A) -> C, g: (B) -> D, v: K2<Validated.T, A, B>): Validated<C, D> {
    return when (val validated = Validated.narrow(v)) {
      is Validated.Invalid -> Validated.Invalid(f(validated.error))
      is Validated.Valid -> Validated.Valid(g(validated.value))
    }
  }

  override fun <A, B, C> first(f: (A) -> C, v: K2<Validated.T, A, B>): Validated<C, B> = Validated.narrow(super.first(f, v))
  override fun <A, B, D> second(g: (B) -> D, v: K2<Validated.T, A, B>): Validated<A, D> = Validated.narrow(super.second(g, v))
}
