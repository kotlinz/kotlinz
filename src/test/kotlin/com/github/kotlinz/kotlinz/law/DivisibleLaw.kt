package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.divisible.Divisible
import io.kotest.matchers.booleans.shouldBeTrue

class DivisibleLaw<T>(val divisible: Divisible<T>, val eq: (K1<T, Int>, K1<T, Int>) -> Boolean) {
    private val delta = { a: Int -> Pair(a, a) }

    fun rightIdentity(m: K1<T, Int>): Boolean =
            eq(divisible.divide(delta, m, divisible.conquer()), m)

    fun leftIdentity(m: K1<T, Int>): Boolean =
            eq(divisible.divide(delta, divisible.conquer(), m), m)

    fun associativity(m: K1<T, Int>, n: K1<T, Int>, o: K1<T, Int>): Boolean =
            eq(
                    divisible.divide(delta, divisible.divide(delta, m, n), o),
                    divisible.divide(delta, m, divisible.divide(delta, n, o))
            )

    fun assertSatisfyingDivisibleLaw(m: K1<T, Int>, n: K1<T, Int>, o: K1<T, Int>) {
        rightIdentity(m).shouldBeTrue()
        leftIdentity(m).shouldBeTrue()
        associativity(m, n, o).shouldBeTrue()
    }
}
