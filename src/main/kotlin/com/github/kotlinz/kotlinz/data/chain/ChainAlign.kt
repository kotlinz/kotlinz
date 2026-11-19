package com.github.kotlinz.kotlinz.data.chain

import com.github.kotlinz.kotlinz.type.align.Align

/** Align instance for [Chain] with the empty chain as the empty structure. */
interface ChainAlign: Align<Chain.T>, ChainSemialign {
  override fun <A> nil(): Chain<A> = Chain.empty()
}
