package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.law.MonadErrorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TryMonadErrorTest : DescribeSpec({
    describe("Try") {
        val error = IllegalStateException("error")

        context("as MonadError") {
            it("should satisfy laws") { MonadErrorLaw(object : TryMonadError {}).assertSatisfyingMonadErrorLaw(error, 1) }
        }

        context("handleErrorWith") {
            it("should recover from Failure") {
                Try.raiseError<Int>(error) handleErrorWith { Try.pure(0) } shouldBe Try.Success(0)
            }

            it("should recover from an exception thrown by fmap") {
                Try.pure(1) fmap { it / 0 } handleError { -1 } shouldBe Try.Success(-1)
            }
        }

        context("attempt") {
            it("should expose the error as Left") {
                Try.raiseError<Int>(error).attempt() shouldBe Try.Success<Either<Throwable, Int>>(Either.Left(error))
            }
        }
    }
})
