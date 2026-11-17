package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.ComonadLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun force(v: Any?): Any? = if (v is Function0<*>) force(v.run()) else v
private fun function0Eq(a: Any?, b: Any?): Boolean = force(a) == force(b)

class Function0Test : DescribeSpec({
    describe("Function0") {
        val monad = object : Function0Monad {}
        val m = Function0 { 3 }
        val f = { x: Int -> Function0 { x + 1 } }
        val g = { x: Int -> Function0 { x * 2 } }

        context("as Functor") {
            it("should satisfy laws") { FunctorLaw(monad, ::function0Eq).assertSatisfyingFunctorLaw(m, { it + 1 }, { it * 2 }) }
        }

        context("as Applicative") {
            val u = Function0 { { s: String -> s.length } }
            val v = Function0 { { i: Int -> i.toString() } }

            it("should satisfy laws") { ApplicativeLaw(monad, ::function0Eq).assertSatisfyingApplicativeLaw(m, 1, u, v) { it.toString() } }
        }

        context("as Monad") {
            it("should satisfy left identity") { monad.bind(f, monad.pure(1)).run() shouldBe f(1).run() }
            it("should satisfy right identity") { monad.bind({ monad.pure(it) }, m).run() shouldBe m.run() }

            it("should satisfy associativity") {
                monad.bind(g, monad.bind(f, m)).run() shouldBe monad.bind({ x: Int -> monad.bind(g, f(x)) }, m).run()
            }

            it("should support fx") {
                Function0.fx { m.bind() + Function0 { 4 }.bind() }() shouldBe 7
            }
        }

        context("as Comonad") {
            it("should satisfy laws") { ComonadLaw(object : Function0Comonad {}, ::function0Eq).assertSatisfyingComonadLaw(m) }
            it("should extend with the whole computation") { m.extend { Function0.narrow(it).run() * 10 }.extract() shouldBe 30 }
        }

        context("laziness") {
            it("should defer evaluation until run") {
                var count = 0
                val lazy = Function0 { count += 1; count } fmap { it * 10 }
                count shouldBe 0
                lazy() shouldBe 10
                lazy() shouldBe 20
            }
        }
    }
})
