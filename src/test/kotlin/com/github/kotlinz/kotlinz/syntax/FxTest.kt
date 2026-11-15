package com.github.kotlinz.kotlinz.syntax

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.eval.Eval
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.monoid.ListMonoid
import com.github.kotlinz.kotlinz.data.monoid.toListMonoid
import com.github.kotlinz.kotlinz.data.reader.Reader
import com.github.kotlinz.kotlinz.data.reader.ask
import com.github.kotlinz.kotlinz.data.rws.RWS
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.data.state.get
import com.github.kotlinz.kotlinz.data.state.put
import com.github.kotlinz.kotlinz.data.trying.Try
import com.github.kotlinz.kotlinz.data.writer.Writer
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class FxTest : DescribeSpec({
    describe("fx") {
        it("should bind Identity values") {
            Identity.fx { Identity(1).bind() + Identity(2).bind() } shouldBe Identity(3)
        }

        it("should bind Just values") {
            Maybe.fx { Maybe.Just(1).bind() + Maybe.Just(2).bind() } shouldBe Maybe.Just(3)
        }

        it("should short-circuit on None") {
            var reached = false
            val result = Maybe.fx {
                val a = Maybe.Just(1).bind()
                val b = Maybe.None<Int>().bind()
                reached = true
                a + b
            }
            result shouldBe Maybe.None<Int>()
            reached shouldBe false
        }

        it("should short-circuit on Left") {
            val result = Either.fx<String, Int> {
                val a = Either.Right<String, Int>(1).bind()
                val b = Either.Left<String, Int>("error").bind()
                a + b
            }
            result shouldBe Either.Left<String, Int>("error")
        }

        it("should turn exceptions into Failure") {
            Try.fx<Int> { Try.pure(1).bind() / 0 }.shouldBeInstanceOf<Try.Failure<Int>>()
        }

        it("should bind Eval values lazily") {
            var evaluated = false
            val result = Eval.fx { Eval.later { evaluated = true; 1 }.bind() + 1 }
            evaluated shouldBe false
            result.value shouldBe 2
        }

        it("should defer IO effects and allow running repeatedly") {
            var count = 0
            val io = IO.fx { IO { count += 1; count }.bind() * 10 }
            count shouldBe 0
            io.unsafeRun() shouldBe 10
            io.unsafeRun() shouldBe 20
        }

        it("should read the environment") {
            Reader.fx<Int, Int> { ask<Int>().bind() * 2 }.run(21) shouldBe 42
        }

        it("should accumulate Writer output") {
            val result = Writer.fx(ListMonoid::class.java) {
                val a = Writer(ListMonoid::class.java, Pair(1, listOf("a").toListMonoid())).bind()
                val b = Writer(ListMonoid::class.java, Pair(2, listOf("b").toListMonoid())).bind()
                a + b
            }
            result.run shouldBe Pair(3, listOf("a", "b").toListMonoid())
        }

        it("should thread State and allow running repeatedly") {
            val counter = State.fx<Int, Int> {
                val n = get<Int>().bind()
                put(n + 1).bind()
                n * 10
            }
            counter.run(1) shouldBe Pair(10, 2)
            counter.run(5) shouldBe Pair(50, 6)
        }

        it("should combine reader, writer and state in RWS") {
            val type = ListMonoid::class.java
            val program = RWS.fx<Int, ListMonoid, Int, Int>(type) {
                val r = RWS.ask<Int, ListMonoid, Int>(type).bind()
                val s = RWS.get<Int, ListMonoid, Int>(type).bind()
                RWS.tell<Int, ListMonoid, Int>(type, listOf(r + s).toListMonoid()).bind()
                RWS.put<Int, ListMonoid, Int>(type, s * 2).bind()
                r + s
            }
            program.run(1, 2) shouldBe Triple(3, 4, listOf(3).toListMonoid())
        }
    }
})
