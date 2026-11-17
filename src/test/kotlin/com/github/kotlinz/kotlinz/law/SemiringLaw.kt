package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.ring.Semiring
import io.kotest.matchers.booleans.shouldBeTrue

class SemiringLaw<T, S: Semiring<T, S>>(val clazz: Class<S>) {
    fun plusAssociativity(a: S, b: S, c: S) = plus(plus(a, b), c) == plus(a, plus(b, c))
    fun plusCommutativity(a: S, b: S) = plus(a, b) == plus(b, a)
    fun plusIdentity(a: S) = plus(zero(), a) == a && plus(a, zero()) == a

    fun timesAssociativity(a: S, b: S, c: S) = times(times(a, b), c) == times(a, times(b, c))
    fun timesIdentity(a: S) = times(one(), a) == a && times(a, one()) == a

    fun leftDistributivity(a: S, b: S, c: S) = times(a, plus(b, c)) == plus(times(a, b), times(a, c))
    fun rightDistributivity(a: S, b: S, c: S) = times(plus(a, b), c) == plus(times(a, c), times(b, c))
    fun annihilation(a: S) = times(zero(), a) == zero() && times(a, zero()) == zero()

    fun assertSatisfyingSemiringLaw(a: S, b: S, c: S) {
        listOf(
                plusAssociativity(a, b, c), plusCommutativity(a, b), plusIdentity(a),
                timesAssociativity(a, b, c), timesIdentity(a),
                leftDistributivity(a, b, c), rightDistributivity(a, b, c), annihilation(a)
        ).forEach { it.shouldBeTrue() }
    }

    private fun plus(a: S, b: S) = Semiring.plus(clazz, a, b)
    private fun times(a: S, b: S) = Semiring.times(clazz, a, b)
    private fun zero() = Semiring.zero(clazz)
    private fun one() = Semiring.one(clazz)
}
