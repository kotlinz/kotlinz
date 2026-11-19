package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.type.eq.Eq

/** [Eq] instance for [Maybe]: [Maybe.None] equals [Maybe.None], and [Maybe.Just] values are compared with [eqA]. */
interface MaybeEq<A>: Eq<Maybe<A>> {
  val eqA: Eq<A>

  override fun eqv(a: Maybe<A>, b: Maybe<A>): Boolean = when {
    a is Maybe.Just && b is Maybe.Just -> eqA.eqv(a.value, b.value)
    else -> a is Maybe.None && b is Maybe.None
  }
}

/** Returns the [Eq] instance for [Maybe] derived from the given element instances. */
fun <A> Maybe.Companion.eq(eqA: Eq<A>): Eq<Maybe<A>> = object: MaybeEq<A> { override val eqA = eqA }
