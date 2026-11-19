package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.type.align.Semialign

/** Semialign instance for [ListK]; the result is as long as the longer list. */
interface ListKSemialign: Semialign<ListK.T>, ListKFunctor {
  override fun <A, B, C> alignWith(f: (Ior<A, B>) -> C, a: K1<ListK.T, A>, b: K1<ListK.T, B>): ListK<C> =
    ListK(alignLists(ListK.narrow(a).value, ListK.narrow(b).value).map(f))

  override fun <A, B> align(a: K1<ListK.T, A>, b: K1<ListK.T, B>): ListK<Ior<A, B>> = ListK.narrow(super.align(a, b))
}

/** Pairs up [xs] and [ys] by index, keeping the leftover elements of the longer list. */
internal fun <A, B> alignLists(xs: List<A>, ys: List<B>): List<Ior<A, B>> =
  (0 until maxOf(xs.size, ys.size)).map { i -> iorAt(xs, ys, i) }

private fun <A, B> iorAt(xs: List<A>, ys: List<B>, i: Int): Ior<A, B> = when {
  i >= ys.size -> Ior.left(xs[i])
  i >= xs.size -> Ior.right(ys[i])
  else -> Ior.both(xs[i], ys[i])
}
