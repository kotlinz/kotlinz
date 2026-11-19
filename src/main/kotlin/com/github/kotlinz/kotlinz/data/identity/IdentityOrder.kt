package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.type.order.Order

/** [Order] instance for [Identity]: compares the wrapped values with [orderA]. */
interface IdentityOrder<A>: Order<Identity<A>> {
  val orderA: Order<A>

  override fun compare(a: Identity<A>, b: Identity<A>): Int = orderA.compare(a.value, b.value)
}

/** Returns the [Order] instance for [Identity] derived from the given element instances. */
fun <A> Identity.Companion.order(orderA: Order<A>): Order<Identity<A>> = object: IdentityOrder<A> { override val orderA = orderA }
