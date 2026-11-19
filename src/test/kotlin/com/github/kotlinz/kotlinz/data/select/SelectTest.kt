package com.github.kotlinz.kotlinz.data.select

import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.law.ApplicativeLaw
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun <A> argmax(xs: List<A>): Select<Int, A> = Select { k -> xs.maxBy(k) }

private val score = { x: Int -> -(x - 4) * (x - 4) }

@Suppress("UNCHECKED_CAST")
private fun observe(v: Any?): Int = (v as Select<Int, Int>).run(score)

class SelectTest : DescribeSpec({
    describe("Select") {
        val monad = object : SelectMonad<Int> {}
        val v = argmax(listOf(1, 2, 3))
        val f = { x: Int -> argmax(listOf(x, x * 2, x * 3)) }
        val g = { x: Int -> argmax(listOf(x - 1, x + 1)) }

        context("as Monad") {
            it("should satisfy laws") { ObservedMonadLaw(monad) { observe(it) }.assertSatisfyingMonadLaw(1, v, f, g) }
        }

        context("as Functor") {
            it("should satisfy laws") {
                FunctorLaw(monad) { a, b -> observe(a) == observe(b) }.assertSatisfyingFunctorLaw(v, { it + 1 }, { it * 2 })
            }
        }

        context("as Applicative") {
            val u = argmax(listOf({ x: Int -> x + 1 }, { x: Int -> x * 3 }))
            val w = argmax(listOf({ x: Int -> x - 2 }, { x: Int -> x * x }))
            it("should satisfy laws") {
                ApplicativeLaw(monad) { a, b -> observe(a) == observe(b) }.assertSatisfyingApplicativeLaw(v, 2, u, w) { it + 5 }
            }
        }

        context("with primitives") {
            it("should pick the best candidate") { v.run(score) shouldBe 3 }

            it("should choose earlier values knowing the continuation") {
                val pair = argmax(listOf(1, 2, 3)) bind { x -> argmax(listOf(1, 2, 3)) fmap { y -> Pair(x, y) } }
                pair.run { (x, y) -> x - y } shouldBe Pair(3, 1)
                pair.run { (x, y) -> -(x + y - 4) * (x + y - 4) - x } shouldBe Pair(1, 3)
            }

            it("should apply a selected function") {
                (v ap argmax(listOf({ x: Int -> x + 10 }, { x: Int -> -x }))).run { -it } shouldBe -3
            }

            it("should support fx") {
                val program = Select.fx<Int, Int> {
                    val x = argmax(listOf(1, 2, 3)).bind()
                    val y = argmax(listOf(10, 20)).bind()
                    x * y
                }
                program.run { -(it - 40) * (it - 40) } shouldBe 40
            }

            it("should convert to SelectT over Identity") {
                v.toSelectT().run { Identity(score(it)) } shouldBe Identity(3)
            }
        }
    }
})
