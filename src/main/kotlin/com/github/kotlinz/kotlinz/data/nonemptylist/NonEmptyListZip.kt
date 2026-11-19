package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.align.Zip

/** Zip instance for [NonEmptyList]; the result is as long as the shorter list. */
interface NonEmptyListZip: Zip<NonEmptyList.T>, NonEmptyListSemialign {
  override fun <A, B, C> zipWith(f: (A, B) -> C, a: K1<NonEmptyList.T, A>, b: K1<NonEmptyList.T, B>): NonEmptyList<C> {
    val left = NonEmptyList.narrow(a)
    val right = NonEmptyList.narrow(b)
    return NonEmptyList(f(left.head, right.head), left.tail.zip(right.tail, f))
  }

  override fun <A, B> zip(a: K1<NonEmptyList.T, A>, b: K1<NonEmptyList.T, B>): NonEmptyList<Pair<A, B>> = NonEmptyList.narrow(super.zip(a, b))
}
