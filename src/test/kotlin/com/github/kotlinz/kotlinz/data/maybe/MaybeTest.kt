package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.law.MonadLaw
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object MaybeTest : Spek({
    describe("Maybe") {
        context("as Monad") {
            val law = MonadLaw(object : MaybeMonad {})

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
