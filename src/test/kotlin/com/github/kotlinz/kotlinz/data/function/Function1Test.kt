package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.ProfunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val inputs = listOf(-2, 0, 1, 5)

@Suppress("UNCHECKED_CAST")
private fun function1Eq(a: Any?, b: Any?): Boolean =
    inputs.all { (a as Function1<Int, *>).run(it) == (b as Function1<Int, *>).run(it) }

class Function1Test : DescribeSpec({
    describe("Function1") {
        val monad = object : Function1Monad<Int> {}
        val arrow = object : Function1Arrow {}
        val m = Function1 { r: Int -> r + 3 }
        val f = { x: Int -> Function1 { r: Int -> x * r } }
        val g = { x: Int -> Function1 { r: Int -> x - r } }

        context("as Functor") {
            it("should satisfy laws") { FunctorLaw(monad, ::function1Eq).assertSatisfyingFunctorLaw(m, { it + 1 }, { it * 2 }) }
        }

        context("as Applicative") {
            val u = Function1 { r: Int -> { s: String -> s.length + r } }
            val v = Function1 { r: Int -> { i: Int -> (i * r).toString() } }

            it("should satisfy laws") { ApplicativeLaw(monad, ::function1Eq).assertSatisfyingApplicativeLaw(m, 1, u, v) { it.toString() } }
        }

        context("as Monad") {
            it("should satisfy left identity") { function1Eq(monad.bind(f, monad.pure(1)), f(1)) shouldBe true }
            it("should satisfy right identity") { function1Eq(monad.bind({ monad.pure(it) }, m), m) shouldBe true }

            it("should satisfy associativity") {
                function1Eq(monad.bind(g, monad.bind(f, m)), monad.bind({ x: Int -> monad.bind(g, f(x)) }, m)) shouldBe true
            }

            it("should support fx") {
                Function1.fx<Int, Int> { m.bind() * Function1 { r: Int -> r }.bind() }(2) shouldBe 10
            }
        }

        context("as Category") {
            it("should satisfy identity laws") {
                function1Eq(arrow.compose(arrow.id(), m), m) shouldBe true
                function1Eq(arrow.compose(m, arrow.id()), m) shouldBe true
            }

            it("should satisfy associativity") {
                val h = Function1 { x: Int -> x * x }
                val k = Function1 { x: Int -> x - 1 }
                function1Eq(arrow.compose(h, arrow.compose(k, m)), arrow.compose(arrow.compose(h, k), m)) shouldBe true
            }
        }

        context("as Profunctor") {
            it("should satisfy laws") { ProfunctorLaw(object : Function1Profunctor {}, ::function1Eq).assertSatisfyingProfunctorLaw(m) }
            it("should map input and output") { m.dimap({ s: String -> s.length }, { it * 2 })("abc") shouldBe 12 }
        }

        context("as Arrow") {
            val pairs = listOf(Pair(1, "a"), Pair(-4, "b"))

            it("should lift the identity function to id") { function1Eq(arrow.arr { x: Int -> x }, arrow.id<Int>()) shouldBe true }

            it("should preserve composition with arr") {
                val left = arrow.arr { x: Int -> (x + 1) * 2 }
                val right = arrow.compose(arrow.arr { x: Int -> x * 2 }, arrow.arr { x: Int -> x + 1 })
                function1Eq(left, right) shouldBe true
            }

            it("should satisfy first(arr f) == arr(first f)") {
                val left = arrow.first<Int, Int, String>(arrow.arr { x: Int -> x + 1 })
                val right = arrow.arr { p: Pair<Int, String> -> Pair(p.first + 1, p.second) }
                pairs.forEach { left(it) shouldBe right(it) }
            }

            it("should split and combine functions") {
                (m split Function1 { s: String -> s.length })(Pair(1, "abc")) shouldBe Pair(4, 3)
                (m combine Function1 { r: Int -> r * 2 })(5) shouldBe Pair(8, 10)
                (m andThen Function1 { x: Int -> x * 10 })(1) shouldBe 40
                m.second<String>()(Pair("x", 1)) shouldBe Pair("x", 4)
            }
        }
    }
})
