package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.ring.Ring
import com.github.kotlinz.kotlinz.type.ring.Semiring
import io.kotest.matchers.booleans.shouldBeTrue

class RingLaw<T, R: Ring<T, R>>(val clazz: Class<R>) {
    fun additiveInverse(a: R) = plus(a, negate(a)) == zero() && plus(negate(a), a) == zero()
    fun subtraction(a: R, b: R) = Ring.minus(clazz, a, b) == plus(a, negate(b))

    fun assertSatisfyingRingLaw(a: R, b: R, c: R) {
        SemiringLaw(clazz).assertSatisfyingSemiringLaw(a, b, c)
        listOf(additiveInverse(a), additiveInverse(b), subtraction(a, b), subtraction(b, c)).forEach { it.shouldBeTrue() }
    }

    private fun plus(a: R, b: R) = Semiring.plus(clazz, a, b)
    private fun negate(a: R) = Ring.negate(clazz, a)
    private fun zero() = Semiring.zero(clazz)
}
