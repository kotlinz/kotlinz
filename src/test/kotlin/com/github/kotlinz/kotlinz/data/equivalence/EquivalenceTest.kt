package com.github.kotlinz.kotlinz.data.equivalence

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.law.ContravariantLaw
import com.github.kotlinz.kotlinz.law.DecidableLaw
import com.github.kotlinz.kotlinz.law.DivisibleLaw
import com.github.kotlinz.kotlinz.data.either.Either
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EquivalenceTest : DescribeSpec({
    describe("Equivalence") {
        context("as Contravariant") {
            val law = ContravariantLaw(object : EquivalenceContravariant {}, ::sameEquivalence)

            it("should satisfy laws") { law.assertSatisfyingContravariantLaw(Equivalence { a: Int, b: Int -> a % 3 == b % 3 }) }

            it("should compare the mapped values") {
                val sameLength = Equivalence.default<Int>() contramap { s: String -> s.length }
                sameLength.equiv("ab", "cd") shouldBe true
                sameLength.equiv("ab", "c") shouldBe false
            }
        }

        context("as Divisible and Decidable") {
            val decidable = object : EquivalenceDecidable {}
            val m = Equivalence { a: Int, b: Int -> a % 2 == b % 2 }
            val n = Equivalence { a: Int, b: Int -> a % 3 == b % 3 }
            val o = Equivalence { a: Int, b: Int -> (a > 0) == (b > 0) }

            it("should satisfy Divisible laws") { DivisibleLaw(decidable, ::sameEquivalence).assertSatisfyingDivisibleLaw(m, n, o) }
            it("should satisfy Decidable laws") { DecidableLaw(decidable, ::sameEquivalence).assertSatisfyingDecidableLaw(m, n, o) }

            it("should require both parts to be equivalent with divide") {
                val e = decidable.divide({ s: String -> Pair(s.length, s.first()) }, Equivalence.default(), Equivalence.default<Char>())
                e.equiv("ab", "ac") shouldBe true
                e.equiv("ab", "bb") shouldBe false
            }

            it("should never equate different branches with choose") {
                val e = decidable.choose({ a: Int -> if (a < 0) Either.Left<Int, Int>(a) else Either.Right(a) }, Equivalence { _: Int, _: Int -> true }, Equivalence { _: Int, _: Int -> true })
                e.equiv(-1, -2) shouldBe true
                e.equiv(-1, 1) shouldBe false
            }
        }
    }
})

private fun sameEquivalence(a: K1<Equivalence.T, Int>, b: K1<Equivalence.T, Int>): Boolean =
        (-5..5).all { x -> (-5..5).all { y -> Equivalence.narrow(a).equiv(x, y) == Equivalence.narrow(b).equiv(x, y) } }
