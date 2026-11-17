package com.github.kotlinz.kotlinz.data.predicate

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.law.ContravariantLaw
import com.github.kotlinz.kotlinz.law.DecidableLaw
import com.github.kotlinz.kotlinz.law.DivisibleLaw
import com.github.kotlinz.kotlinz.data.either.Either
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

        context("as Divisible and Decidable") {
            val decidable = object : PredicateDecidable {}
            val (m, n, o) = Triple(Predicate { a: Int -> a > 0 }, Predicate { a: Int -> a % 2 == 0 }, Predicate { a: Int -> a < 5 })

            it("should satisfy Divisible laws") { DivisibleLaw(decidable, ::samePredicate).assertSatisfyingDivisibleLaw(m, n, o) }
            it("should satisfy Decidable laws") { DecidableLaw(decidable, ::samePredicate).assertSatisfyingDecidableLaw(m, n, o) }

            it("should require both parts with divide") {
                val p = decidable.divide({ s: String -> Pair(s.length, s) }, m, Predicate { s: String -> s.startsWith("k") })
                p.test("kt") shouldBe true
                p.test("") shouldBe false
                p.test("java") shouldBe false
            }

            it("should test the selected branch with choose") {
                val p = decidable.choose({ a: Int -> if (a < 0) Either.Left<Int, String>(a) else Either.Right(a.toString()) }, Predicate { a: Int -> a < -5 }, Predicate { s: String -> s.length == 1 })
                p.test(-10) shouldBe true
                p.test(-1) shouldBe false
                p.test(3) shouldBe true
                p.test(30) shouldBe false
            }
        }
    }
})

private fun samePredicate(a: K1<Predicate.T, Int>, b: K1<Predicate.T, Int>): Boolean =
        (-10..10).all { Predicate.narrow(a).test(it) == Predicate.narrow(b).test(it) }
