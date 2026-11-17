package com.github.kotlinz.kotlinz.type.transformation

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.maybe.Maybe

object MaybeToListK: NaturalTransformation<Maybe.T, ListK.T> {
  override fun <A> invoke(fa: K1<Maybe.T, A>): ListK<A> = when (val maybe = Maybe.narrow(fa)) {
    is Maybe.Just -> ListK(listOf(maybe.value))
    is Maybe.None -> ListK(emptyList())
  }
}
