package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.type.eq.Eq

/** [Eq] instance for [Tuple2]: both components equivalent under [eqA] and [eqB]. */
interface Tuple2Eq<A, B>: Eq<Tuple2<A, B>> {
  val eqA: Eq<A>
  val eqB: Eq<B>

  override fun eqv(a: Tuple2<A, B>, b: Tuple2<A, B>): Boolean = eqA.eqv(a.first, b.first) && eqB.eqv(a.second, b.second)
}

/** Returns the [Eq] instance for [Tuple2] derived from the given element instances. */
fun <A, B> Tuple2.Companion.eq(eqA: Eq<A>, eqB: Eq<B>): Eq<Tuple2<A, B>> = object: Tuple2Eq<A, B> { override val eqA = eqA; override val eqB = eqB }
