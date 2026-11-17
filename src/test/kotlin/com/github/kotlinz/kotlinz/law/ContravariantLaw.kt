package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Contravariant
import io.kotest.matchers.booleans.shouldBeTrue

class ContravariantLaw<T>(val contravariant: Contravariant<T>, val eq: (K1<T, Int>, K1<T, Int>) -> Boolean) {
    fun identity(v: K1<T, Int>): Boolean =
            eq(contravariant.contramap({ a: Int -> a }, v), v)

    fun composition(v: K1<T, Int>, f: (Int) -> Int, g: (Int) -> Int): Boolean =
            eq(contravariant.contramap(f, contravariant.contramap(g, v)), contravariant.contramap({ a: Int -> g(f(a)) }, v))

    fun assertSatisfyingContravariantLaw(v: K1<T, Int>) {
        val f = { a: Int -> a + 1 }
        val g = { a: Int -> a * 3 }

        identity(v).shouldBeTrue()
        composition(v, f, g).shouldBeTrue()
    }
}
