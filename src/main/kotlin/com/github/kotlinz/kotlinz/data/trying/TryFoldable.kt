package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

/** Foldable instance for [Try]; a [Try.Failure] folds as empty. */
interface TryFoldable: Foldable<Try.T> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<Try.T, A>): B {
    val t = Try.narrow(v)
    return when (t) {
      is Try.Success -> f(initial, t.value)
      is Try.Failure -> initial
    }
  }

  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<Try.T, A>): B =
    foldl(initial, { acc: B, a: A -> f(a, acc) }, v)
}
