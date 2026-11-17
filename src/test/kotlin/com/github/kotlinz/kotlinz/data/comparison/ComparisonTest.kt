package com.github.kotlinz.kotlinz.data.comparison

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.law.ContravariantLaw
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
    }
})

private fun sameComparison(a: K1<Comparison.T, Int>, b: K1<Comparison.T, Int>): Boolean =
        (-5..5).all { x -> (-5..5).all { y -> Comparison.narrow(a).compare(x, y) == Comparison.narrow(b).compare(x, y) } }
