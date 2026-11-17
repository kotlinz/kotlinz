package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Profunctor
import io.kotest.matchers.booleans.shouldBeTrue

class ProfunctorLaw<T, F>(val profunctor: Profunctor<T, F>, val eq: (K3<T, F, Int, Int>, K3<T, F, Int, Int>) -> Boolean) {
    fun identity(v: K3<T, F, Int, Int>): Boolean =
            eq(profunctor.dimap({ a: Int -> a }, { a: Int -> a }, v), v)

    fun composition(v: K3<T, F, Int, Int>, f1: (Int) -> Int, g1: (Int) -> Int, f2: (Int) -> Int, g2: (Int) -> Int): Boolean =
            eq(
                    profunctor.dimap(f2, g2, profunctor.dimap(f1, g1, v)),
                    profunctor.dimap({ a: Int -> f1(f2(a)) }, { a: Int -> g2(g1(a)) }, v)
            )

    fun dimapConsistency(v: K3<T, F, Int, Int>, f: (Int) -> Int, g: (Int) -> Int): Boolean =
            eq(profunctor.dimap(f, g, v), profunctor.lmap(f, profunctor.rmap(g, v)))

    fun assertSatisfyingProfunctorLaw(v: K3<T, F, Int, Int>) {
        val (f1, g1) = Pair({ a: Int -> a + 1 }, { a: Int -> a * 2 })
        val (f2, g2) = Pair({ a: Int -> a - 3 }, { a: Int -> a * a })

        identity(v).shouldBeTrue()
        composition(v, f1, g1, f2, g2).shouldBeTrue()
        dimapConsistency(v, f1, g1).shouldBeTrue()
    }
}
