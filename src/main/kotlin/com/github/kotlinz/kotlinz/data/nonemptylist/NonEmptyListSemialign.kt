package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.listk.alignLists
import com.github.kotlinz.kotlinz.type.align.Semialign

/** Semialign instance for [NonEmptyList]; the result is as long as the longer list. */
interface NonEmptyListSemialign: Semialign<NonEmptyList.T>, NonEmptyListFunctor {
  override fun <A, B, C> alignWith(f: (Ior<A, B>) -> C, a: K1<NonEmptyList.T, A>, b: K1<NonEmptyList.T, B>): NonEmptyList<C> {
    val left = NonEmptyList.narrow(a)
    val right = NonEmptyList.narrow(b)
    return NonEmptyList(f(Ior.both(left.head, right.head)), alignLists(left.tail, right.tail).map(f))
  }

  override fun <A, B> align(a: K1<NonEmptyList.T, A>, b: K1<NonEmptyList.T, B>): NonEmptyList<Ior<A, B>> = NonEmptyList.narrow(super.align(a, b))
}
