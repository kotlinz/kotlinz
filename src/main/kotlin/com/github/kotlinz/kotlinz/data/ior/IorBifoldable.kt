package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bifoldable

/** Bifoldable instance for [Ior]; [Ior.Both] folds the left value before the right one. */
interface IorBifoldable : Bifoldable<Ior.T> {
  override fun <A, B, C> bifoldLeft(initial: C, f: (C, A) -> C, g: (C, B) -> C, v: K2<Ior.T, A, B>): C {
    return when (val ior = Ior.narrow(v)) {
      is Ior.Left -> f(initial, ior.value)
      is Ior.Right -> g(initial, ior.value)
      is Ior.Both -> g(f(initial, ior.left), ior.right)
    }
  }

  override fun <A, B, C> bifoldRight(initial: C, f: (A, C) -> C, g: (B, C) -> C, v: K2<Ior.T, A, B>): C {
    return when (val ior = Ior.narrow(v)) {
      is Ior.Left -> f(ior.value, initial)
      is Ior.Right -> g(ior.value, initial)
      is Ior.Both -> f(ior.left, g(ior.right, initial))
    }
  }
}
