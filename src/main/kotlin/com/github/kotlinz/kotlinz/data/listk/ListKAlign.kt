package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.type.align.Align

/** Align instance for [ListK] with the empty list as the empty structure. */
interface ListKAlign: Align<ListK.T>, ListKSemialign {
  override fun <A> nil(): ListK<A> = ListK(listOf())
}
