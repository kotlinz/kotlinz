package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.law.MonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class TryTest : DescribeSpec({
    describe("Try") {
        context("as Monad") {
            val law = MonadLaw(object : TryMonad {})

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

        context("of") {
            it("should be Success when no exception is thrown") {
                Try.of { 1 } shouldBe Try.Success(1)
            }

            it("should be Failure when an exception is thrown") {
                Try.of<Int> { throw IllegalStateException() }.getOrElse(0) shouldBe 0
            }
        }

        context("bind") {
            it("should catch an exception thrown by the function") {
                val result = Try.pure(1) bind { x: Int -> Try.pure(x / 0) }
                (result is Try.Failure).shouldBeTrue()
            }
        }
    }
})
