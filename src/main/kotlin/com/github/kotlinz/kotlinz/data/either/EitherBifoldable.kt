package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bifoldable

/** Bifoldable instance for [Either]; folds exactly one value from either side. */
interface EitherBifoldable : Bifoldable<Either.T> {
  override fun <A, B, C> bifoldLeft(initial: C, f: (C, A) -> C, g: (C, B) -> C, v: K2<Either.T, A, B>): C {
    return when (val either = Either.narrow(v)) {
      is Either.Left -> f(initial, either.value)
      is Either.Right -> g(initial, either.value)
    }
  }

  override fun <A, B, C> bifoldRight(initial: C, f: (A, C) -> C, g: (B, C) -> C, v: K2<Either.T, A, B>): C =
    bifoldLeft(initial, { acc: C, a: A -> f(a, acc) }, { acc: C, b: B -> g(b, acc) }, v)
}
