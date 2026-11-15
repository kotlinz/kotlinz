package com.github.kotlinz.kotlinz.data.cont

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ContTest : DescribeSpec({
    describe("Cont") {
        val monad = object : ContMonad<Int> {}
        val f = { x: Int -> Cont.pure<Int, Int>(x + 1) }
        val g = { x: Int -> Cont.pure<Int, Int>(x * 2) }
        val run = { c: Cont<Int, Int> -> c.eval() }

        context("as Monad") {
            it("should satisfy left identity") {
                run(monad.bind(f, monad.pure(3))) shouldBe run(f(3))
            }

            it("should satisfy right identity") {
                val m = Cont.pure<Int, Int>(3)
                run(monad.bind({ monad.pure(it) }, m)) shouldBe run(m)
            }

            it("should satisfy associativity") {
                val m = Cont.pure<Int, Int>(3)
                run(monad.bind(g, monad.bind(f, m))) shouldBe run(monad.bind({ monad.bind(g, f(it)) }, m))
            }
        }

        context("callCC") {
            it("should escape with the value passed to the continuation") {
                val c = callCC<Int, Int, Int> { exit -> exit(10) bind { Cont.pure<Int, Int>(it + 100) } }
                c.eval() shouldBe 10
            }

            it("should continue when the continuation is not called") {
                val c = callCC<Int, Int, Int> { exit -> Cont.pure(5) }
                (c fmap { it * 2 }).eval() shouldBe 10
            }
        }
    }
})
