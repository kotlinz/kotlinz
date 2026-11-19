package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private typealias L = K1<LogicT.T, Identity.T>

class LogicTTest : DescribeSpec({
    describe("LogicT") {
        val instance = object : LogicTMonadLogic<Identity.T> { override val monad = Logic.monad }
        val observe = { v: K1<L, *> -> LogicT.narrow(v).observeAll() }

        context("as Monad") {
            val law = ObservedMonadLaw(instance, observe)
            val f = { x: Int -> Logic.of(x, x + 1) }
            val g = { x: Int -> if (x % 2 == 0) Logic.pure(x * 10) else Logic.empty() }

            it("should satisfy laws with several results") { law.assertSatisfyingMonadLaw(1, Logic.of(1, 2, 3), f, g) }
            it("should satisfy laws with no result") { law.assertSatisfyingMonadLaw(2, Logic.empty(), f, g) }
        }

        context("as Monad over IO") {
            val io = object : IOMonad {}
            val law = ObservedMonadLaw(object : LogicTMonad<IO.T> { override val monad = io }) {
                IO.narrow(LogicT.narrow(it).observeAllT()).unsafeRun()
            }
            val f = { x: Int -> LogicT.lift(io, IO { x + 1 }) orElse LogicT.pure(io, x) }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, LogicT.of(io, 1, 2), f, f) }
        }

        context("as MonadPlus") {
            val a = Logic.of(1, 2)
            val b = Logic.of(3)
            val c = Logic.empty<Int>()

            it("should satisfy Alternative laws") {
                listOf(a, b, c).forEach { m ->
                    observe(instance.orElse(instance.empty(), m)) shouldBe observe(m)
                    observe(instance.orElse(m, instance.empty())) shouldBe observe(m)
                }
                observe(instance.orElse(instance.orElse(a, b), c)) shouldBe observe(instance.orElse(a, instance.orElse(b, c)))
            }

            it("should satisfy zero laws") {
                observe(instance.bind({ x: Int -> Logic.pure(x) }, instance.mzero<Int>())) shouldBe listOf()
                observe(instance.bind({ _: Int -> instance.mzero<Int>() }, a)) shouldBe listOf()
            }
        }

        context("as MonadLogic") {
            it("should split an empty search into None") {
                Logic.empty<Int>().msplit().observeAll() shouldBe listOf(Maybe.None())
            }

            it("should split off the first result") {
                val split = (Logic.pure(1) orElse Logic.of(2, 3)).msplit().observeAll().single() as Maybe.Just
                split.value.first shouldBe 1
                observe(split.value.second) shouldBe listOf(2, 3)
            }
        }
    }
})
