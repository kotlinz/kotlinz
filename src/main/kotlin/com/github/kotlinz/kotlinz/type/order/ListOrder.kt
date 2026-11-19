package com.github.kotlinz.kotlinz.type.order

/** Lexicographic [Order] instance for [List] using [orderA]; a proper prefix is smaller. */
interface ListOrder<A>: Order<List<A>> {
  val orderA: Order<A>

  override fun compare(a: List<A>, b: List<A>): Int = a.asSequence().zip(b.asSequence())
    .map { (x, y) -> orderA.compare(x, y) }
    .firstOrNull { it != 0 } ?: a.size.compareTo(b.size)
}
