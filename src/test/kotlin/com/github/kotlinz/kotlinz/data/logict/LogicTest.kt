package com.github.kotlinz.kotlinz.data.logict

import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonadIO
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun nats(n: Int): LogicT<Identity.T, Int> = Logic.pure(n) orElse Logic.defer { nats(n + 1) }
private fun repeatOf(x: Int): LogicT<Identity.T, Int> = Logic.pure(x) orElse Logic.defer { repeatOf(x) }

class LogicTest : DescribeSpec({
    describe("Logic") {
        context("observation") {
            it("should observe the first, some or all results") {
                Logic.of(1, 2, 3).observe() shouldBe Maybe.Just(1)
                Logic.empty<Int>().observe() shouldBe Maybe.None()
                Logic.of(1, 2, 3).observeMany(2) shouldBe listOf(1, 2)
                Logic.of(1, 2, 3).observeAll() shouldBe listOf(1, 2, 3)
            }

            it("should take from infinite searches lazily") {
                nats(0).observeMany(5) shouldBe listOf(0, 1, 2, 3, 4)
                (nats(0) bind { if (it % 3 == 0) Logic.pure(it) else Logic.empty() }).observeMany(3) shouldBe listOf(0, 3, 6)
            }
        }

        context("fairness") {
            it("should interleave infinite searches") {
                (repeatOf(1) orElse repeatOf(2)).observeMany(4) shouldBe listOf(1, 1, 1, 1)
                (repeatOf(1) interleave repeatOf(2)).observeMany(4) shouldBe listOf(1, 2, 1, 2)
            }

            it("should fairly bind into infinite searches") {
                val shifted = { x: Int -> nats(x * 100) }
                (Logic.of(0, 1) fairBind shifted).observeMany(4) shouldBe listOf(0, 100, 1, 101)
                (Logic.of(0, 1) bind shifted).observeMany(4) shouldBe listOf(0, 1, 2, 3)
            }
        }

        context("pruning") {
            it("should branch with ifte") {
                Logic.of(1, 2).ifte({ Logic.pure(it * 10) }, Logic.pure(0)).observeAll() shouldBe listOf(10, 20)
                Logic.empty<Int>().ifte({ Logic.pure(it * 10) }, Logic.pure(0)).observeAll() shouldBe listOf(0)
            }

            it("should keep the first result with once, even of an infinite search") {
                nats(7).once().observeAll() shouldBe listOf(7)
            }

            it("should negate with lnot") {
                Logic.empty<Int>().lnot().observeAll() shouldBe listOf(Unit)
                nats(0).lnot().observeAll() shouldBe listOf()
            }
        }

        context("fx") {
            it("should enumerate combinations with replay") {
                val triples = Logic.fx {
                    val a = Logic.of(1, 2, 3, 4, 5).bind()
                    val b = Logic.of(1, 2, 3, 4, 5).bind()
                    val c = Logic.of(1, 2, 3, 4, 5).bind()
                    if (a <= b && a * a + b * b == c * c) Logic.pure(Unit).bind() else Logic.empty<Unit>().bind()
                    Triple(a, b, c)
                }
                triples.observeAll() shouldBe listOf(Triple(3, 4, 5))
            }
        }

        context("over IO") {
            it("should defer effects until observed and stop early") {
                val io = object : IOMonadIO {}
                var runs = 0
                val tick = LogicT.liftIO(io, IO { runs += 1; runs })
                val search = tick orElse tick orElse tick
                runs shouldBe 0
                IO.narrow(search.observeManyT(2)).unsafeRun() shouldBe listOf(1, 2)
                runs shouldBe 2
            }
        }
    }
})
