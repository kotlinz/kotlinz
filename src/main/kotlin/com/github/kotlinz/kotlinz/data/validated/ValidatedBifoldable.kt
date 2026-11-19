package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bifoldable

/** Bifoldable instance for [Validated]; the error is the first argument and the valid value the second. */
interface ValidatedBifoldable : Bifoldable<Validated.T> {
  override fun <A, B, C> bifoldLeft(initial: C, f: (C, A) -> C, g: (C, B) -> C, v: K2<Validated.T, A, B>): C {
    return when (val validated = Validated.narrow(v)) {
      is Validated.Invalid -> f(initial, validated.error)
      is Validated.Valid -> g(initial, validated.value)
    }
  }

  override fun <A, B, C> bifoldRight(initial: C, f: (A, C) -> C, g: (B, C) -> C, v: K2<Validated.T, A, B>): C =
    bifoldLeft(initial, { acc: C, a: A -> f(a, acc) }, { acc: C, b: B -> g(b, acc) }, v)
}
