package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Alternative

/** Alternative instance for [LazyList]: [empty] is the empty list and [orElse] concatenates lazily. */
interface LazyListAlternative: Alternative<LazyList.T>, LazyListApplicative {
  override fun <A> empty(): LazyList<A> = LazyList.empty()

  override fun <A> orElse(v1: K1<LazyList.T, A>, v2: K1<LazyList.T, A>): LazyList<A> =
    LazyList.narrow(v1).concat(LazyList.narrow(v2))
}
