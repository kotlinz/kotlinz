package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.group.Band
import com.github.kotlinz.kotlinz.type.group.Semigroup
import io.kotest.matchers.booleans.shouldBeTrue

class BandLaw<T, B: Band<T, B>>(val clazz: Class<B>) {
    fun idempotency(a: B): Boolean =
            Semigroup.op(clazz, a, a) == a

    fun associativity(a: B, b: B, c: B) =
            Semigroup.op(clazz, Semigroup.op(clazz, a, b), c) == Semigroup.op(clazz, a, Semigroup.op(clazz, b, c))

    fun assertSatisfyingBandLaw(a: B, b: B, c: B) {
        listOf(a, b, c).forEach { idempotency(it).shouldBeTrue() }
        associativity(a, b, c).shouldBeTrue()
    }
}
