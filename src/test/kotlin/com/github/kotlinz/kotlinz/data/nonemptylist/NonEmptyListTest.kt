package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.law.MonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class NonEmptyListTest : DescribeSpec({
    describe("NonEmptyList") {
        context("as Monad") {
            val law = MonadLaw(object : NonEmptyListMonad {})

            context("with Int.MAX_VALUE") {
                it("should satisfy laws") { law.assertSatisfyingMonadLaw(Int.MAX_VALUE) }
            }

            context("with Int.MIN_VALUE") {
                it("should satisfy laws") { law.assertSatisfyingMonadLaw(Int.MIN_VALUE) }
            }

            context("with String") {
                it("should satisfy laws") { law.assertSatisfyingMonadLaw("Monad Law") }
            }

            it("should bind every element") {
                nonEmptyListOf(1, 2) bind { nonEmptyListOf(it, it * 10) } shouldBe nonEmptyListOf(1, 10, 2, 20)
            }
        }

        context("as Comonad") {
            it("should extract head") {
                nonEmptyListOf(1, 2, 3).extract() shouldBe 1
            }

            it("should extend over suffixes") {
                val sums = nonEmptyListOf(1, 2, 3).extend { NonEmptyList.narrow(it).toList().sum() }
                sums shouldBe nonEmptyListOf(6, 5, 3)
            }
        }
    }
})
