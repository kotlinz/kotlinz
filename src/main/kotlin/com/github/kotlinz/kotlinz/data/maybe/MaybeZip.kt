package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.align.Zip

/** Zip instance for [Maybe]; the result is [Maybe.Just] only when both sides are. */
interface MaybeZip: Zip<Maybe.T>, MaybeSemialign {
  override fun <A, B, C> zipWith(f: (A, B) -> C, a: K1<Maybe.T, A>, b: K1<Maybe.T, B>): Maybe<C> {
    val left = Maybe.narrow(a)
    val right = Maybe.narrow(b)
    return if (left is Maybe.Just && right is Maybe.Just) Maybe.Just(f(left.value, right.value)) else Maybe.None()
  }

  override fun <A, B> zip(a: K1<Maybe.T, A>, b: K1<Maybe.T, B>): Maybe<Pair<A, B>> = Maybe.narrow(super.zip(a, b))
}
