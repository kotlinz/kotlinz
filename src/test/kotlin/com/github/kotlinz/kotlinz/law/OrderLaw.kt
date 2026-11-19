package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.order.Order
import io.kotest.matchers.booleans.shouldBeTrue

class OrderLaw<A>(val order: Order<A>) {
    fun reflexivity(a: A) = order.lteqv(a, a)
    fun antisymmetry(a: A, b: A) = !(order.lteqv(a, b) && order.lteqv(b, a)) || order.eqv(a, b)
    fun transitivity(a: A, b: A, c: A) = !(order.lteqv(a, b) && order.lteqv(b, c)) || order.lteqv(a, c)
    fun totality(a: A, b: A) = order.lteqv(a, b) || order.lteqv(b, a)
    fun compareAntisymmetry(a: A, b: A) = Integer.signum(order.compare(a, b)) == -Integer.signum(order.compare(b, a))
    fun minMaxConsistency(a: A, b: A) = order.lteqv(order.min(a, b), order.max(a, b)) &&
        (order.eqv(order.min(a, b), a) || order.eqv(order.min(a, b), b))

    fun assertSatisfyingOrderLaw(a: A, b: A, c: A) {
        EqLaw(order).assertSatisfyingEqLaw(a, b, c)
        val values = listOf(a, b, c)
        values.forEach { reflexivity(it).shouldBeTrue() }
        values.flatMap { x -> values.map { y -> Pair(x, y) } }.forEach { (x, y) ->
            listOf(antisymmetry(x, y), totality(x, y), compareAntisymmetry(x, y), minMaxConsistency(x, y))
                .forEach { it.shouldBeTrue() }
            values.forEach { z -> transitivity(x, y, z).shouldBeTrue() }
        }
    }
}
