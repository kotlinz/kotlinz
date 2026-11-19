package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.type.align.Semialign

/** Semialign instance for [Maybe]; the result is [Maybe.None] only when both sides are. */
interface MaybeSemialign: Semialign<Maybe.T>, MaybeFunctor {
  override fun <A, B, C> alignWith(f: (Ior<A, B>) -> C, a: K1<Maybe.T, A>, b: K1<Maybe.T, B>): Maybe<C> {
    val left = Maybe.narrow(a)
    val right = Maybe.narrow(b)
    return when {
      left is Maybe.Just && right is Maybe.Just -> Maybe.Just(f(Ior.both(left.value, right.value)))
      left is Maybe.Just -> Maybe.Just(f(Ior.left(left.value)))
      right is Maybe.Just -> Maybe.Just(f(Ior.right(right.value)))
      else -> Maybe.None()
    }
  }

  override fun <A, B> align(a: K1<Maybe.T, A>, b: K1<Maybe.T, B>): Maybe<Ior<A, B>> = Maybe.narrow(super.align(a, b))
}
