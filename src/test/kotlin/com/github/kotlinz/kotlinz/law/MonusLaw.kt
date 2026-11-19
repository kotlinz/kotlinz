package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.group.Monus
import io.kotest.matchers.booleans.shouldBeTrue

class MonusLaw<T, M: Monus<T, M>>(val clazz: Class<M>) {
    fun commutativeMonus(a: M, b: M) = op(a, monus(b, a)) == op(b, monus(a, b))
    fun repeatedMonus(a: M, b: M, c: M) = monus(monus(a, b), c) == monus(a, op(b, c))
    fun selfMonus(a: M) = monus(a, a) == zero()
    fun zeroMonus(a: M) = monus(zero(), a) == zero()

    fun assertSatisfyingMonusLaw(a: M, b: M, c: M) {
        CommutativeMonoidLaw(clazz).assertSatisfyingCommutativeMonoidLaw(a, b, c)
        val values = listOf(a, b, c)
        values.forEach { listOf(selfMonus(it), zeroMonus(it)).forEach { law -> law.shouldBeTrue() } }
        values.forEach { x -> values.forEach { y ->
            commutativeMonus(x, y).shouldBeTrue()
            values.forEach { z -> repeatedMonus(x, y, z).shouldBeTrue() }
        } }
    }

    private fun op(a: M, b: M) = Monoid.op(clazz, a, b)
    private fun monus(a: M, b: M) = Monus.monus(clazz, a, b)
    private fun zero() = Monoid.mzero(clazz)
}
