package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bifoldable

/** Bifoldable instance for [Tuple2]; folds the first component before the second. */
interface Tuple2Bifoldable : Bifoldable<Tuple2.T> {
  override fun <A, B, C> bifoldLeft(initial: C, f: (C, A) -> C, g: (C, B) -> C, v: K2<Tuple2.T, A, B>): C {
    val tuple = Tuple2.narrow(v)
    return g(f(initial, tuple.first), tuple.second)
  }

  override fun <A, B, C> bifoldRight(initial: C, f: (A, C) -> C, g: (B, C) -> C, v: K2<Tuple2.T, A, B>): C {
    val tuple = Tuple2.narrow(v)
    return f(tuple.first, g(tuple.second, initial))
  }
}
