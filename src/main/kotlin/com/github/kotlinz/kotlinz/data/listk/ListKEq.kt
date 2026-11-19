package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.type.eq.Eq

/** [Eq] instance for [ListK]: element-wise equivalence under [eqA]. */
interface ListKEq<A>: Eq<ListK<A>> {
  val eqA: Eq<A>

  override fun eqv(a: ListK<A>, b: ListK<A>): Boolean = Eq.list(eqA).eqv(a.value, b.value)
}

/** Returns the [Eq] instance for [ListK] derived from the given element instances. */
fun <A> ListK.Companion.eq(eqA: Eq<A>): Eq<ListK<A>> = object: ListKEq<A> { override val eqA = eqA }
