package com.github.kotlinz.kotlinz.data.equivalence

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.law.ContravariantLaw
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
    }
})

private fun sameEquivalence(a: K1<Equivalence.T, Int>, b: K1<Equivalence.T, Int>): Boolean =
        (-5..5).all { x -> (-5..5).all { y -> Equivalence.narrow(a).equiv(x, y) == Equivalence.narrow(b).equiv(x, y) } }
