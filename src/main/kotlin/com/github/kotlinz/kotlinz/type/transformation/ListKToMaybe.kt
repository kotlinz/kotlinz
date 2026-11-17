package com.github.kotlinz.kotlinz.type.transformation

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.maybe.Maybe

object ListKToMaybe: NaturalTransformation<ListK.T, Maybe.T> {
  override fun <A> invoke(fa: K1<ListK.T, A>): Maybe<A> {
    val list = ListK.narrow(fa).value
    return if (list.isEmpty()) Maybe.None() else Maybe.Just(list.first())
  }
}
