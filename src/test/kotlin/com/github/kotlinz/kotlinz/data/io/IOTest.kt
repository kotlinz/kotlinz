package com.github.kotlinz.kotlinz.data.io

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class IOTest : DescribeSpec({
    describe("IO") {
        context("as Monad") {
            val f = { x: Int -> IO.pure(x + 1) }
            val g = { x: Int -> IO.pure(x * 2) }

            it("should satisfy left identity") {
                (IO.pure(1) bind f).unsafeRun() shouldBe f(1).unsafeRun()
            }

            it("should satisfy right identity") {
                (IO.pure(1) bind { IO.pure(it) }).unsafeRun() shouldBe 1
            }

            it("should satisfy associativity") {
                val m = IO.pure(1)
                (m bind f bind g).unsafeRun() shouldBe (m bind { f(it) bind g }).unsafeRun()
            }
        }

        context("unsafeRun") {
            it("should defer side effects until run") {
                var count = 0
                val io = IO { count += 1 } fmap { count }
                count shouldBe 0
                io.unsafeRun() shouldBe 1
                io.unsafeRun() shouldBe 2
            }
        }
    }
})
