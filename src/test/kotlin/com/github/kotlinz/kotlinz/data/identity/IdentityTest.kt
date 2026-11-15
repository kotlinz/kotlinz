package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.law.MonadLaw
import io.kotest.core.spec.style.DescribeSpec

class IdentityTest : DescribeSpec({
    describe("Identity") {
        context("as Monad") {
            val law = MonadLaw(object : IdentityMonad {})

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
    }
})