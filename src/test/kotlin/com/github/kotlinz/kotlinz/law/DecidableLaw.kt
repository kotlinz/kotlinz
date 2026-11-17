package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.divisible.Decidable
import io.kotest.matchers.booleans.shouldBeTrue

class DecidableLaw<T>(val decidable: Decidable<T>, val eq: (K1<T, Int>, K1<T, Int>) -> Boolean) {
    private val absurd = decidable.lose<Int> { throw IllegalStateException("lose must never be evaluated") }

    fun leftIdentity(m: K1<T, Int>): Boolean =
            eq(decidable.choose({ a: Int -> Either.Left<Int, Int>(a) }, m, absurd), m)

    fun rightIdentity(m: K1<T, Int>): Boolean =
            eq(decidable.choose({ a: Int -> Either.Right<Int, Int>(a) }, absurd, m), m)

    fun associativity(m: K1<T, Int>, n: K1<T, Int>, o: K1<T, Int>): Boolean =
            eq(
                    decidable.choose(route { it < 2 }, decidable.choose(route { it == 0 }, m, n), o),
                    decidable.choose(route { it == 0 }, m, decidable.choose(route { it == 1 }, n, o))
            )

    fun assertSatisfyingDecidableLaw(m: K1<T, Int>, n: K1<T, Int>, o: K1<T, Int>) {
        leftIdentity(m).shouldBeTrue()
        rightIdentity(m).shouldBeTrue()
        associativity(m, n, o).shouldBeTrue()
    }

    private fun route(toLeft: (Int) -> Boolean): (Int) -> Either<Int, Int> =
            { a -> if (toLeft(Math.floorMod(a, 3))) Either.Left(a) else Either.Right(a) }
}
