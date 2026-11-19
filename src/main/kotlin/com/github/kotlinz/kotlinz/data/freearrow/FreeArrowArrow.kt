package com.github.kotlinz.kotlinz.data.freearrow

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.arrow.Arrow

/** Arrow instance for [FreeArrow]. */
interface FreeArrowArrow<P>: Arrow<FreeArrow.T, P>, FreeArrowCategory<P> {
  override fun <A, B> arr(f: (A) -> B): FreeArrow<P, A, B> = FreeArrow.Arr(f)

  override fun <A, B, C> first(f: K3<FreeArrow.T, P, A, B>): FreeArrow<P, Pair<A, C>, Pair<B, C>> = FreeArrow.First(FreeArrow.narrow(f))
}
