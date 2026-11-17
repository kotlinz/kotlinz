package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

interface EitherFoldable<S>: Foldable<K1<Either.T, S>> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<K1<Either.T, S>, A>): B {
    val either = Either.narrow(v)
    return when (either) {
      is Either.Left -> initial
      is Either.Right -> f(initial, either.value)
    }
  }

  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<K1<Either.T, S>, A>): B =
    foldl(initial, { acc: B, a: A -> f(a, acc) }, v)
}
