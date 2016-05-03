package data.maybe

import K1
import type.applicative.Applicative

interface MaybeApplicative: Applicative<Maybe.µ> {
  override fun <B> pure(v: B): Maybe<B> = Maybe.Just(v)

  override fun <A, B> ap(f: K1<Maybe.µ, (A) -> B>, v: K1<Maybe.µ, A>): Maybe<B> {
    val maybe = Maybe.narrow(v)
    val maybef = Maybe.narrow(f)
    return when {
      maybe is Maybe.Just && maybef is Maybe.Just -> Maybe.narrow(fmap(maybef.value, maybe))
      else -> Maybe.None()
    }
  }
}
