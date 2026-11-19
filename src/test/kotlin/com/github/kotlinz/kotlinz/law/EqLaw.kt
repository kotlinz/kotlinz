package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.eq.Eq
import io.kotest.matchers.booleans.shouldBeTrue

class EqLaw<A>(val eq: Eq<A>) {
    fun reflexivity(a: A) = eq.eqv(a, a)
    fun symmetry(a: A, b: A) = eq.eqv(a, b) == eq.eqv(b, a)
    fun transitivity(a: A, b: A, c: A) = !(eq.eqv(a, b) && eq.eqv(b, c)) || eq.eqv(a, c)
    fun neqvConsistency(a: A, b: A) = eq.neqv(a, b) == !eq.eqv(a, b)

    fun assertSatisfyingEqLaw(a: A, b: A, c: A) {
        listOf(a, b, c).forEach { reflexivity(it).shouldBeTrue() }
        pairs(a, b, c).forEach { (x, y) ->
            symmetry(x, y).shouldBeTrue()
            neqvConsistency(x, y).shouldBeTrue()
        }
        triples(a, b, c).forEach { (x, y, z) -> transitivity(x, y, z).shouldBeTrue() }
    }

    private fun pairs(a: A, b: A, c: A) = listOf(a, b, c).flatMap { x -> listOf(a, b, c).map { y -> Pair(x, y) } }
    private fun triples(a: A, b: A, c: A) = pairs(a, b, c).flatMap { (x, y) -> listOf(a, b, c).map { z -> Triple(x, y, z) } }
}
