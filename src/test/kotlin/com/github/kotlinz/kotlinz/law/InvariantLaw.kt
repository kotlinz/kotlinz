package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Invariant
import io.kotest.matchers.booleans.shouldBeTrue

class InvariantLaw<T>(val invariant: Invariant<T>, val eq: (K1<T, Int>, K1<T, Int>) -> Boolean = { a, b -> a == b }) {
    fun identity(v: K1<T, Int>): Boolean =
            eq(invariant.imap({ a: Int -> a }, { a: Int -> a }, v), v)

    fun composition(v: K1<T, Int>, f1: (Int) -> Int, g1: (Int) -> Int, f2: (Int) -> Int, g2: (Int) -> Int): Boolean =
            eq(
                    invariant.imap(f2, g2, invariant.imap(f1, g1, v)),
                    invariant.imap({ a: Int -> f2(f1(a)) }, { a: Int -> g1(g2(a)) }, v)
            )

    fun assertSatisfyingInvariantLaw(v: K1<T, Int>) {
        identity(v).shouldBeTrue()
        composition(v, { it + 1 }, { it - 1 }, { it * 2 }, { it / 2 }).shouldBeTrue()
    }
}
