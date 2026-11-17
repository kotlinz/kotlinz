package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ValidatedTest : DescribeSpec({
    describe("Validated") {
        val type = StringMonoid::class.java
        val applicative = object : ValidatedApplicative<StringMonoid> { override val type = type }
        val valid = Validated.valid<StringMonoid, Int>(3)
        val invalid = Validated.invalid<StringMonoid, Int>(StringMonoid("e1"))
        val u = Validated.invalid<StringMonoid, (String) -> Int>(StringMonoid("u"))
        val v = Validated.valid<StringMonoid, (Int) -> String> { it.toString() }
        val vInvalid = Validated.invalid<StringMonoid, (Int) -> String>(StringMonoid("v"))

        context("as Functor") {
            val law = FunctorLaw(object : ValidatedFunctor<StringMonoid> {})

            it("should satisfy laws with Valid") { law.assertSatisfyingFunctorLaw(valid, { it + 1 }, { it * 2 }) }
            it("should satisfy laws with Invalid") { law.assertSatisfyingFunctorLaw(invalid, { it + 1 }, { it * 2 }) }
        }

        context("as Applicative") {
            val law = ApplicativeLaw(applicative)

            it("should satisfy laws with Valid") { law.assertSatisfyingApplicativeLaw(valid, 1, u, v) { it.toString() } }
            it("should satisfy laws with Invalid") { law.assertSatisfyingApplicativeLaw(invalid, 1, u, vInvalid) { it.toString() } }
        }

        context("error accumulation") {
            val add = Validated.liftA2<StringMonoid, Int, Int, Int>(type) { a, b -> a + b }

            it("should combine valid values") {
                add(valid, valid) shouldBe Validated.valid(6)
            }

            it("should accumulate errors in order") {
                add(invalid, Validated.invalid(StringMonoid("e2"))) shouldBe Validated.invalid(StringMonoid("e1e2"))
            }

            it("should accumulate errors with liftA3") {
                val sum3 = Validated.liftA3<StringMonoid, Int, Int, Int, Int>(type) { a, b, c -> a + b + c }
                sum3(invalid, valid, Validated.invalid(StringMonoid("e3"))) shouldBe Validated.invalid(StringMonoid("e1e3"))
            }

            it("should apply with ap") {
                valid.ap(type, Validated.valid { x: Int -> x * 10 }) shouldBe Validated.valid(30)
            }
        }

        context("conversion") {
            it("should convert to Either") {
                valid.toEither() shouldBe Either.Right<StringMonoid, Int>(3)
                invalid.toEither() shouldBe Either.Left<StringMonoid, Int>(StringMonoid("e1"))
            }

            it("should convert from Either") {
                Either.Right<String, Int>(1).toValidated() shouldBe Validated.valid(1)
                Either.Left<String, Int>("e").toValidated() shouldBe Validated.invalid("e")
            }
        }
    }
})
