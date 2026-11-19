package com.github.kotlinz.kotlinz.type.eq

/** [Eq] instance for [List]: equal sizes and pairwise equivalent elements under [eqA]. */
interface ListEq<A>: Eq<List<A>> {
  val eqA: Eq<A>

  override fun eqv(a: List<A>, b: List<A>): Boolean =
    a.size == b.size && a.zip(b).all { (x, y) -> eqA.eqv(x, y) }
}
