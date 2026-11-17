package com.github.kotlinz.kotlinz.data.predicate

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.law.ContravariantLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class PredicateTest : DescribeSpec({
    describe("Predicate") {
        context("as Contravariant") {
            val law = ContravariantLaw(object : PredicateContravariant {}, ::samePredicate)

            it("should satisfy laws") { law.assertSatisfyingContravariantLaw(Predicate { a: Int -> a % 2 == 0 }) }

            it("should test the mapped value") {
                val isLong = Predicate { n: Int -> n > 3 } contramap { s: String -> s.length }
                isLong.test("kotlinz") shouldBe true
                isLong.test("kt") shouldBe false
            }
        }
    }
})

private fun samePredicate(a: K1<Predicate.T, Int>, b: K1<Predicate.T, Int>): Boolean =
        (-10..10).all { Predicate.narrow(a).test(it) == Predicate.narrow(b).test(it) }
