package com.github.kotlinz.kotlinz.data.lazylist

import com.github.kotlinz.kotlinz.type.align.Align

/** Align instance for [LazyList] with the empty list as the empty structure. */
interface LazyListAlign: Align<LazyList.T>, LazyListSemialign {
  override fun <A> nil(): LazyList<A> = LazyList.empty()
}
