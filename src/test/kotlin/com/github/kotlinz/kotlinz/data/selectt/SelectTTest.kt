package com.github.kotlinz.kotlinz.data.selectt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.MonadTransLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val maybe = object : MaybeMonad {}

private fun Maybe<Int>.orMin(): Int = if (this is Maybe.Just) value else Int.MIN_VALUE

private fun <A> argmax(xs: List<A>): SelectT<Int, Maybe.T, A> =
    SelectT(maybe) { k -> Maybe.Just(xs.maxBy { Maybe.narrow(k(it)).orMin() }) }

private val score = { x: Int -> if (x > 10) Maybe.None<Int>() else Maybe.Just(-(x - 4) * (x - 4)) }

@Suppress("UNCHECKED_CAST")
private fun observe(v: K1<*, *>): Maybe<Int> = Maybe.narrow((v as SelectT<Int, Maybe.T, Int>).run(score))

class SelectTTest : DescribeSpec({
    describe("SelectT") {
        val instance = SelectTMonadTrans.of<Int, Maybe.T>(maybe)
        val v = argmax(listOf(1, 2, 3))
        val f = { x: Int -> argmax(listOf(x, x * 2, x * 3)) }
        val g = { x: Int -> if (x > 5) SelectT.lift(maybe, Maybe.None()) else argmax(listOf(x - 1, x + 1)) }

        context("as Monad over Maybe") {
            it("should satisfy laws") { ObservedMonadLaw(instance) { observe(it) }.assertSatisfyingMonadLaw(1, v, f, g) }
        }

        context("as Functor") {
            it("should satisfy laws") {
                FunctorLaw(instance) { a, b -> observe(a) == observe(b) }.assertSatisfyingFunctorLaw(v, { it + 1 }, { it * 2 })
            }
        }

        context("as MonadTrans") {
            it("should satisfy laws") {
                MonadTransLaw(maybe, instance) { observe(it) }.assertSatisfyingMonadTransLaw(2, Maybe.Just(3)) { x -> Maybe.Just(x * 2) }
            }
        }

        context("with primitives") {
            it("should choose earlier values knowing the continuation") {
                val pair = argmax(listOf(1, 2, 3)) bind { x -> argmax(listOf(1, 2, 3)) fmap { y -> Pair(x, y) } }
                pair.run { (x, y) -> Maybe.Just(x - y) } shouldBe Maybe.Just(Pair(3, 1))
            }

            it("should propagate failure of the inner monad") {
                (v bind { SelectT.lift<Int, Maybe.T, Int>(maybe, Maybe.None()) }).run(score) shouldBe Maybe.None<Int>()
            }

            it("should apply a selected function") {
                (v ap argmax(listOf({ x: Int -> x + 10 }, { x: Int -> -x }))).run { Maybe.Just(-it) } shouldBe Maybe.Just(-3)
            }

            it("should support fx") {
                val program = SelectT.fx<Int, Maybe.T, Int>(maybe) {
                    val x = argmax(listOf(1, 2, 3)).bind()
                    val y = SelectT.lift<Int, Maybe.T, Int>(maybe, Maybe.Just(10)).bind()
                    x * y
                }
                program.run { Maybe.Just(-(it - 20) * (it - 20)) } shouldBe Maybe.Just(20)
            }
        }
    }
})
