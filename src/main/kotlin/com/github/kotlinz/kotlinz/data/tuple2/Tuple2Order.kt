package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.type.order.Order

/** Lexicographic [Order] instance for [Tuple2]: compares `first` with [orderA], then `second` with [orderB]. */
interface Tuple2Order<A, B>: Order<Tuple2<A, B>> {
  val orderA: Order<A>
  val orderB: Order<B>

  override fun compare(a: Tuple2<A, B>, b: Tuple2<A, B>): Int = orderA.compare(a.first, b.first).takeIf { it != 0 } ?: orderB.compare(a.second, b.second)
}

/** Returns the [Order] instance for [Tuple2] derived from the given element instances. */
fun <A, B> Tuple2.Companion.order(orderA: Order<A>, orderB: Order<B>): Order<Tuple2<A, B>> = object: Tuple2Order<A, B> { override val orderA = orderA; override val orderB = orderB }
