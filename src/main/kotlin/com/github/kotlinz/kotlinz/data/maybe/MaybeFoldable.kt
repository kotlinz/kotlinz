package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

interface MaybeFoldable: Foldable<Maybe.T> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<Maybe.T, A>): B {
    val maybe = Maybe.narrow(v)
    return when (maybe) {
      is Maybe.Just -> f(initial, maybe.value)
      is Maybe.None -> initial
    }
  }

  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<Maybe.T, A>): B =
    foldl(initial, { acc: B, a: A -> f(a, acc) }, v)
}
