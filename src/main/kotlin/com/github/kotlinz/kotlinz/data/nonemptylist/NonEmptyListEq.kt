package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.type.eq.Eq

/** [Eq] instance for [NonEmptyList]: element-wise equivalence under [eqA]. */
interface NonEmptyListEq<A>: Eq<NonEmptyList<A>> {
  val eqA: Eq<A>

  override fun eqv(a: NonEmptyList<A>, b: NonEmptyList<A>): Boolean = Eq.list(eqA).eqv(a.toList(), b.toList())
}

/** Returns the [Eq] instance for [NonEmptyList] derived from the given element instances. */
fun <A> NonEmptyList.Companion.eq(eqA: Eq<A>): Eq<NonEmptyList<A>> = object: NonEmptyListEq<A> { override val eqA = eqA }
