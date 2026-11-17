package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.law.MonadErrorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EitherMonadErrorTest : DescribeSpec({
    describe("Either") {
        val monad = object : EitherMonadError<String> {}

        context("as MonadError") {
            it("should satisfy laws") { MonadErrorLaw(monad).assertSatisfyingMonadErrorLaw("error", 1) }
        }

        context("handleErrorWith") {
            it("should recover from Left") {
                Either.raiseError<String, Int>("error") handleErrorWith { Either.Right(it.length) } shouldBe Either.Right<String, Int>(5)
            }

            it("should keep Right") {
                Either.Right<String, Int>(1) handleError { it.length } shouldBe Either.Right<String, Int>(1)
            }
        }

        context("ensure") {
            it("should raise when the predicate fails") {
                monad.ensure({ x: Int -> "$x is odd" }, { it % 2 == 0 }, Either.Right(3)) shouldBe Either.Left<String, Int>("3 is odd")
            }
        }

        context("rethrow") {
            it("should flatten a nested Left") {
                monad.rethrow(Either.Right<String, Either<String, Int>>(Either.Left("inner"))) shouldBe Either.Left<String, Int>("inner")
            }
        }
    }
})
