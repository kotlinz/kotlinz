package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Alternative

/** Alternative instance for [ListK]: [empty] is the empty list and [orElse] concatenates. */
interface ListKAlternative: Alternative<ListK.T>, ListKApplicative {
  override fun <A> empty(): ListK<A> = ListK(listOf())

  override fun <A> orElse(v1: K1<ListK.T, A>, v2: K1<ListK.T, A>): ListK<A> {
    return ListK(ListK.narrow(v1).value + ListK.narrow(v2).value)
  }
}
