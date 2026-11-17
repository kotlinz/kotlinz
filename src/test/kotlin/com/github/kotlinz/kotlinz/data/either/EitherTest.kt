package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.law.BifunctorLaw
import com.github.kotlinz.kotlinz.law.MonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EitherTest : DescribeSpec({
    describe("Either") {
        context("as Monad") {
            val law = MonadLaw(object : EitherMonad<Int> {})

            context("with Int.MAX_VALUE") {
                it("should satisfy laws") { law.assertSatisfyingMonadLaw(Int.MAX_VALUE) }
            }

            context("with Int.MIN_VALUE") {
                it("should satisfy laws") { law.assertSatisfyingMonadLaw(Int.MIN_VALUE) }
            }

            context("with String") {
                it("should satisfy laws") { law.assertSatisfyingMonadLaw("Monad Law") }
            }
        }

        context("as Bifunctor") {
            val law = BifunctorLaw(object : EitherBifunctor {})
            val f = { s: String -> s + "!" }
            val g = { s: String -> s.uppercase() }
            val h = { i: Int -> i + 1 }
            val i = { i: Int -> i * 2 }

            it("should satisfy laws with Left") { law.assertSatisfyingBifunctorLaw(Either.Left<String, Int>("left"), f, g, h, i) }
            it("should satisfy laws with Right") { law.assertSatisfyingBifunctorLaw(Either.Right<String, Int>(1), f, g, h, i) }

            it("should map the left side only with first") {
                (Either.Left<String, Int>("a") first g) shouldBe Either.Left<String, Int>("A")
                (Either.Right<String, Int>(1) first g) shouldBe Either.Right<String, Int>(1)
            }

            it("should map the right side only with second") {
                (Either.Right<String, Int>(1) second h) shouldBe Either.Right<String, Int>(2)
                (Either.Left<String, Int>("a") second h) shouldBe Either.Left<String, Int>("a")
            }
        }
    }
})

