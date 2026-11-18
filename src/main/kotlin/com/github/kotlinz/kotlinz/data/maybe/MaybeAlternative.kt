package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Alternative

/** Alternative instance for [Maybe]: [empty] is [Maybe.None] and [orElse] picks the first [Maybe.Just]. */
interface MaybeAlternative: Alternative<Maybe.T>, MaybeApplicative {
  override fun <A> empty(): Maybe<A> = Maybe.None()

  override fun <A> orElse(v1: K1<Maybe.T, A>, v2: K1<Maybe.T, A>): Maybe<A> {
    val maybe = Maybe.narrow(v1)
    return when (maybe) {
      is Maybe.Just -> maybe
      is Maybe.None -> Maybe.narrow(v2)
    }
  }
}
