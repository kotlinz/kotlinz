package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.type.order.Order

/** [Order] instance for [Maybe]: [Maybe.None] is smaller than any [Maybe.Just], and [Maybe.Just] values are compared with [orderA]. */
interface MaybeOrder<A>: Order<Maybe<A>> {
  val orderA: Order<A>

  override fun compare(a: Maybe<A>, b: Maybe<A>): Int = when {
    a is Maybe.Just && b is Maybe.Just -> orderA.compare(a.value, b.value)
    else -> (a is Maybe.Just).compareTo(b is Maybe.Just)
  }
}

/** Returns the [Order] instance for [Maybe] derived from the given element instances. */
fun <A> Maybe.Companion.order(orderA: Order<A>): Order<Maybe<A>> = object: MaybeOrder<A> { override val orderA = orderA }
