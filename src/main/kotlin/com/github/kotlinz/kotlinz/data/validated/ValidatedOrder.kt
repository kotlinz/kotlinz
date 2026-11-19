package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.type.order.Order

/** [Order] instance for [Validated]: every [Validated.Invalid] is smaller than any [Validated.Valid]; contents are compared with [orderE] / [orderA]. */
interface ValidatedOrder<E, A>: Order<Validated<E, A>> {
  val orderE: Order<E>
  val orderA: Order<A>

  override fun compare(a: Validated<E, A>, b: Validated<E, A>): Int = when {
    a is Validated.Invalid && b is Validated.Invalid -> orderE.compare(a.error, b.error)
    a is Validated.Valid && b is Validated.Valid -> orderA.compare(a.value, b.value)
    else -> (a is Validated.Valid).compareTo(b is Validated.Valid)
  }
}

/** Returns the [Order] instance for [Validated] derived from the given element instances. */
fun <E, A> Validated.Companion.order(orderE: Order<E>, orderA: Order<A>): Order<Validated<E, A>> = object: ValidatedOrder<E, A> { override val orderE = orderE; override val orderA = orderA }
