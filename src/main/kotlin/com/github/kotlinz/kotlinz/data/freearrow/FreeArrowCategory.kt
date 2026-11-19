package com.github.kotlinz.kotlinz.data.freearrow

import com.github.kotlinz.kotlinz.type.category.Category

/** Category instance for [FreeArrow]; [id] is the lifted identity function. */
interface FreeArrowCategory<P>: Category<FreeArrow.T, P>, FreeArrowCompose<P> {
  override fun <A> id(): FreeArrow<P, A, A> = FreeArrow.Arr { a: A -> a }
}
