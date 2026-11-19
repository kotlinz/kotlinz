package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.align.Zip

/** Zip instance for [ListK]; the result is as long as the shorter list. */
interface ListKZip: Zip<ListK.T>, ListKSemialign {
  override fun <A, B, C> zipWith(f: (A, B) -> C, a: K1<ListK.T, A>, b: K1<ListK.T, B>): ListK<C> =
    ListK(ListK.narrow(a).value.zip(ListK.narrow(b).value, f))

  override fun <A, B> zip(a: K1<ListK.T, A>, b: K1<ListK.T, B>): ListK<Pair<A, B>> = ListK.narrow(super.zip(a, b))
}
