package com.github.kotlinz.kotlinz.data.indexedstate

import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.law.IxMonadLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private class Opened(val name: String)
private class Closed(val log: List<String>)

class IndexedStateTest : DescribeSpec({
    describe("IndexedState") {
        val ix = object : IndexedStateIxMonad {}

        context("as IxMonad") {
            val law = IxMonadLaw(ix) { (it as IndexedState<Int, *, *>).run(3) }
            val v = IndexedState<Int, String, Int> { i -> Pair(i * 2, "s$i") }
            val f = { x: Int -> IndexedState<String, Int, Int> { s -> Pair(x + s.length, s.length) } }
            val g = { x: Int -> IndexedState<Int, Boolean, String> { i -> Pair("$x/$i", i > x) } }

            it("should satisfy left identity") { law.leftIdentity(1) { x: Int -> IndexedState<Int, String, Int> { i -> Pair(x + i, "$i") } } }
            it("should satisfy right identity") { law.rightIdentity(v) }
            it("should satisfy associativity") { law.associativity(v, f, g) }
        }

        context("as IxFunctor") {
            it("should map the value and keep the state") {
                (IndexedState<Int, String, Int> { i -> Pair(i, "$i") } fmap { it + 1 }).run(1) shouldBe Pair(2, "1")
            }
        }

        context("as Monad when the state type is fixed") {
            val monad = IndexedStateMonad.of<Int>()
            val observe = { v: Any? -> (v as IndexedState<Int, Int, *>).run(3) }
            val f = { x: Int -> IndexedState<Int, Int, Int> { s -> Pair(x + s, s * 2) } }
            val g = { x: Int -> IndexedState<Int, Int, Int> { s -> Pair(x * s, s + 1) } }

            it("should satisfy monad laws") { ObservedMonadLaw(monad, observe).assertSatisfyingMonadLaw(1, IndexedState.get(), f, g) }
            it("should satisfy functor laws") {
                FunctorLaw(monad) { a, b -> observe(a) == observe(b) }.assertSatisfyingFunctorLaw(f(1), { it + 1 }, { it.toString() })
            }
        }

        context("with primitives") {
            it("should change the state type with put") {
                val program = IndexedState.get<Int>() bind { n -> IndexedState.put<Int, String>("n=$n") fmap { n } }
                program.run(1) shouldBe Pair(1, "n=1")
            }

            it("should track protocol steps in the state type") {
                val open = IndexedState.modify<String, Opened> { Opened(it) }
                val close = IndexedState.modify<Opened, Closed> { Closed(listOf("open ${it.name}", "close ${it.name}")) }
                (open bind { close }).exec("file").log shouldBe listOf("open file", "close file")
            }

            it("should apply a wrapped function running it first") {
                val f = IndexedState<Int, String, (Int) -> Int> { i -> Pair({ x: Int -> x + i }, "$i") }
                val v = IndexedState<String, Boolean, Int> { s -> Pair(s.length, s.isEmpty()) }
                (v ap f).run(10) shouldBe Pair(12, false)
            }

            it("should project the state with gets and eval") {
                IndexedState.gets<Int, String> { "v$it" }.eval(4) shouldBe "v4"
            }

            it("should convert to State") {
                IndexedState.get<Int>().toState().run(5) shouldBe Pair(5, 5)
            }

            it("should support fx when the state type is fixed") {
                val program = IndexedState.fx<Int, Int> {
                    val n = IndexedState.get<Int>().bind()
                    IndexedState.put<Int, Int>(n * 10).bind()
                    n + 1
                }
                program.run(2) shouldBe Pair(3, 20)
            }
        }
    }
})
