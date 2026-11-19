package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.type.eq.Eq

/** [Eq] instance for [Identity]: wrapped values equivalent under [eqA]. */
interface IdentityEq<A>: Eq<Identity<A>> {
  val eqA: Eq<A>

  override fun eqv(a: Identity<A>, b: Identity<A>): Boolean = eqA.eqv(a.value, b.value)
}

/** Returns the [Eq] instance for [Identity] derived from the given element instances. */
fun <A> Identity.Companion.eq(eqA: Eq<A>): Eq<Identity<A>> = object: IdentityEq<A> { override val eqA = eqA }
