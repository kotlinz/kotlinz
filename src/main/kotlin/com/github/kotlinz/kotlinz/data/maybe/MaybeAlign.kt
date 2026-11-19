package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.type.align.Align

/** Align instance for [Maybe] with [Maybe.None] as the empty structure. */
interface MaybeAlign: Align<Maybe.T>, MaybeSemialign {
  override fun <A> nil(): Maybe<A> = Maybe.None()
}
