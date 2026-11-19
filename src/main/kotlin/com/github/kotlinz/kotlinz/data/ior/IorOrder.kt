package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.type.order.Order

/** [Order] instance for [Ior]: `Left < Right < Both`; contents of the same case are compared with [orderA] / [orderB] ([Ior.Both] lexicographically). */
interface IorOrder<A, B>: Order<Ior<A, B>> {
  val orderA: Order<A>
  val orderB: Order<B>

  override fun compare(a: Ior<A, B>, b: Ior<A, B>): Int = when {
    a is Ior.Left && b is Ior.Left -> orderA.compare(a.value, b.value)
    a is Ior.Right && b is Ior.Right -> orderB.compare(a.value, b.value)
    a is Ior.Both && b is Ior.Both -> orderA.compare(a.left, b.left).takeIf { it != 0 } ?: orderB.compare(a.right, b.right)
    else -> rank(a).compareTo(rank(b))
  }

  private fun rank(v: Ior<A, B>): Int = when (v) {
    is Ior.Left -> 0
    is Ior.Right -> 1
    is Ior.Both -> 2
  }
}

/** Returns the [Order] instance for [Ior] derived from the given element instances. */
fun <A, B> Ior.Companion.order(orderA: Order<A>, orderB: Order<B>): Order<Ior<A, B>> = object: IorOrder<A, B> { override val orderA = orderA; override val orderB = orderB }
