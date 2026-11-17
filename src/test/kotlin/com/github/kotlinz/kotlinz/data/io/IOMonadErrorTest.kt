package com.github.kotlinz.kotlinz.data.io

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.law.MonadErrorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class IOMonadErrorTest : DescribeSpec({
    describe("IO") {
        val error = IllegalStateException("error")

        context("as MonadError") {
            val law = MonadErrorLaw(object : IOMonadError {}) { runCatching { (it as IO<*>).unsafeRun() } }
            it("should satisfy laws") { law.assertSatisfyingMonadErrorLaw(error, 1) }
        }

        context("raiseError") {
            it("should defer the error until run") {
                val io = IO.raiseError<Int>(error)
                runCatching { io.unsafeRun() }.exceptionOrNull() shouldBe error
            }
        }

        context("handleErrorWith") {
            it("should recover from an exception thrown while running") {
                val io = IO<Int> { throw error } handleErrorWith { IO.pure(0) }
                io.unsafeRun() shouldBe 0
            }
        }

        context("attempt") {
            it("should expose the error as Left") {
                IO<Int> { throw error }.attempt().unsafeRun() shouldBe Either.Left<Throwable, Int>(error)
            }

            it("should wrap the value as Right") {
                IO.pure(1).attempt().unsafeRun() shouldBe Either.Right<Throwable, Int>(1)
            }
        }
    }
})
