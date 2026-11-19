package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.hash.Hash
import io.kotest.matchers.booleans.shouldBeTrue

class HashLaw<A>(val hash: Hash<A>) {
    fun consistency(a: A, b: A) = !hash.eqv(a, b) || hash.hash(a) == hash.hash(b)

    fun assertSatisfyingHashLaw(a: A, b: A, c: A) {
        EqLaw(hash).assertSatisfyingEqLaw(a, b, c)
        val values = listOf(a, b, c)
        values.flatMap { x -> values.map { y -> consistency(x, y) } }.forEach { it.shouldBeTrue() }
    }
}
