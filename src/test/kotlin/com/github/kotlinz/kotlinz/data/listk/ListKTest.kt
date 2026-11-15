package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.law.MonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ListKTest : DescribeSpec({
    describe("ListK") {
        context("as Monad") {
            val law = MonadLaw(object : ListKMonad {})

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
                listKOf(1, 2) bind { listKOf(it, it * 10) } shouldBe listKOf(1, 10, 2, 20)
            }

            it("should apply every function to every value") {
                val fs = listKOf({ x: Int -> x + 1 }, { x: Int -> x * 2 })
                listKOf(1, 2) ap fs shouldBe listKOf(2, 3, 2, 4)
            }
        }

        context("as MonadPlus") {
            it("should concatenate lists") {
                listKOf(1, 2) mplus listKOf(3) shouldBe listKOf(1, 2, 3)
            }

            it("should treat mzero as identity") {
                listKOf(1) mplus ListK.mzero() shouldBe listKOf(1)
            }
        }

        context("as MonadZip") {
            it("should zip lists") {
                listKOf(1, 2, 3) mzip listKOf("a", "b") shouldBe listKOf(Pair(1, "a"), Pair(2, "b"))
            }

            it("should unzip lists") {
                ListK.munzip(listKOf(Pair(1, "a"), Pair(2, "b"))) shouldBe Pair(listKOf(1, 2), listKOf("a", "b"))
            }
        }
    }
})
