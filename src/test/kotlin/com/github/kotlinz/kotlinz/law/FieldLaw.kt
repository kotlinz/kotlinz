package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.ring.Field
import com.github.kotlinz.kotlinz.type.ring.Semiring
import io.kotest.matchers.booleans.shouldBeTrue

class FieldLaw<T, F: Field<T, F>>(val clazz: Class<F>) {
    fun timesCommutativity(a: F, b: F) = times(a, b) == times(b, a)
    fun multiplicativeInverse(a: F) = a == Semiring.zero(clazz) || times(a, Field.reciprocal(clazz, a)) == Semiring.one(clazz)
    fun division(a: F, b: F) = Field.div(clazz, a, b) == times(a, Field.reciprocal(clazz, b))

    fun assertSatisfyingFieldLaw(a: F, b: F, c: F) {
        RingLaw(clazz).assertSatisfyingRingLaw(a, b, c)
        listOf(
                timesCommutativity(a, b), multiplicativeInverse(a), multiplicativeInverse(b), multiplicativeInverse(c),
                division(a, b), division(b, c)
        ).forEach { it.shouldBeTrue() }
    }

    private fun times(a: F, b: F) = Semiring.times(clazz, a, b)
}
