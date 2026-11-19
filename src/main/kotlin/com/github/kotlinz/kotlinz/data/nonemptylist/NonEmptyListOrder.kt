package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.type.order.Order

/** Lexicographic [Order] instance for [NonEmptyList] using [orderA]. */
interface NonEmptyListOrder<A>: Order<NonEmptyList<A>> {
  val orderA: Order<A>

  override fun compare(a: NonEmptyList<A>, b: NonEmptyList<A>): Int = Order.list(orderA).compare(a.toList(), b.toList())
}

/** Returns the [Order] instance for [NonEmptyList] derived from the given element instances. */
fun <A> NonEmptyList.Companion.order(orderA: Order<A>): Order<NonEmptyList<A>> = object: NonEmptyListOrder<A> { override val orderA = orderA }
