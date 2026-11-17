package com.github.kotlinz.kotlinz.data.comparison

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.law.ContravariantLaw
import com.github.kotlinz.kotlinz.law.DecidableLaw
import com.github.kotlinz.kotlinz.law.DivisibleLaw
import com.github.kotlinz.kotlinz.data.either.Either
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ComparisonTest : DescribeSpec({
    describe("Comparison") {
        context("as Contravariant") {
            val law = ContravariantLaw(object : ComparisonContravariant {}, ::sameComparison)

            it("should satisfy laws") { law.assertSatisfyingContravariantLaw(Comparison.natural<Int>()) }

            it("should compare the mapped values") {
                val byLength = Comparison.natural<Int>() contramap { s: String -> s.length }
                listOf("ccc", "a", "bb").sortedWith(byLength.toComparator()) shouldBe listOf("a", "bb", "ccc")
            }
        }

        context("as Divisible and Decidable") {
            val decidable = object : ComparisonDecidable {}
            val m = Comparison.natural<Int>()
            val n = Comparison.natural<Int>() contramap { a: Int -> -a }
            val o = Comparison.natural<Int>() contramap { a: Int -> a % 2 }

            it("should satisfy Divisible laws") { DivisibleLaw(decidable, ::sameComparison).assertSatisfyingDivisibleLaw(o, m, n) }
            it("should satisfy Decidable laws") { DecidableLaw(decidable, ::sameComparison).assertSatisfyingDecidableLaw(m, n, o) }

            it("should compare lexicographically with divide") {
                val byLengthThenValue = decidable.divide({ s: String -> Pair(s.length, s) }, m, Comparison.natural<String>())
                listOf("bb", "a", "ab", "c").sortedWith(byLengthThenValue.toComparator()) shouldBe listOf("a", "c", "ab", "bb")
            }

            it("should order Left before Right with choose") {
                val c = decidable.choose({ a: Int -> if (a < 0) Either.Left<Int, Int>(a) else Either.Right(a) }, n, m)
                listOf(2, -1, 1, -2).sortedWith(c.toComparator()) shouldBe listOf(-1, -2, 1, 2)
            }
        }
    }
})

private fun sameComparison(a: K1<Comparison.T, Int>, b: K1<Comparison.T, Int>): Boolean =
        (-5..5).all { x -> (-5..5).all { y -> Comparison.narrow(a).compare(x, y) == Comparison.narrow(b).compare(x, y) } }
