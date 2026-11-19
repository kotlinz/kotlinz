package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.type.eq.Eq

/** [Eq] instance for [Validated]: same case and equivalent contents under [eqE] / [eqA]. */
interface ValidatedEq<E, A>: Eq<Validated<E, A>> {
  val eqE: Eq<E>
  val eqA: Eq<A>

  override fun eqv(a: Validated<E, A>, b: Validated<E, A>): Boolean = when {
    a is Validated.Invalid && b is Validated.Invalid -> eqE.eqv(a.error, b.error)
    a is Validated.Valid && b is Validated.Valid -> eqA.eqv(a.value, b.value)
    else -> false
  }
}

/** Returns the [Eq] instance for [Validated] derived from the given element instances. */
fun <E, A> Validated.Companion.eq(eqE: Eq<E>, eqA: Eq<A>): Eq<Validated<E, A>> = object: ValidatedEq<E, A> { override val eqE = eqE; override val eqA = eqA }
