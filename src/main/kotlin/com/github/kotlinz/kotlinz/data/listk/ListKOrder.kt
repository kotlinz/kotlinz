package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.type.order.Order

/** Lexicographic [Order] instance for [ListK] using [orderA]. */
interface ListKOrder<A>: Order<ListK<A>> {
  val orderA: Order<A>

  override fun compare(a: ListK<A>, b: ListK<A>): Int = Order.list(orderA).compare(a.value, b.value)
}

/** Returns the [Order] instance for [ListK] derived from the given element instances. */
fun <A> ListK.Companion.order(orderA: Order<A>): Order<ListK<A>> = object: ListKOrder<A> { override val orderA = orderA }
