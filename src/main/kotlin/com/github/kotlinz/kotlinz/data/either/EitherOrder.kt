package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.type.order.Order

/** [Order] instance for [Either]: every [Either.Left] is smaller than any [Either.Right]; values on the same side are compared with [orderL] / [orderR]. */
interface EitherOrder<L, R>: Order<Either<L, R>> {
  val orderL: Order<L>
  val orderR: Order<R>

  override fun compare(a: Either<L, R>, b: Either<L, R>): Int = when {
    a is Either.Left && b is Either.Left -> orderL.compare(a.value, b.value)
    a is Either.Right && b is Either.Right -> orderR.compare(a.value, b.value)
    else -> (a is Either.Right).compareTo(b is Either.Right)
  }
}

/** Returns the [Order] instance for [Either] derived from the given element instances. */
fun <L, R> Either.Companion.order(orderL: Order<L>, orderR: Order<R>): Order<Either<L, R>> = object: EitherOrder<L, R> { override val orderL = orderL; override val orderR = orderR }
