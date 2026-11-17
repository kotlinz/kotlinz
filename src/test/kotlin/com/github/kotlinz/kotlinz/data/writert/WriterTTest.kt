package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.MonadWriterLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class WriterTTest : DescribeSpec({
    describe("WriterT") {
        val type = StringMonoid::class.java
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val io = object : IOMonad {}
        val instance = object : WriterTMonadWriter<Identity.T, StringMonoid> {
            override val monad = identity
            override val type = type
        }
        val observe = { v: Any? -> (v as WriterT<*, *, *>).run }
        val w = { s: String -> StringMonoid(s) }

        context("as Monad over Identity") {
            val law = ObservedMonadLaw(instance, observe)
            val f = { x: Int -> WriterT.writer(identity, type, Pair(x + 1, w("f"))) }
            val g = { x: Int -> WriterT.writer(identity, type, Pair(x * 2, w("g"))) }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, WriterT.writer(identity, type, Pair(1, w("m"))), f, g) }
        }

        context("as Monad over IO") {
            val monad = object : WriterTMonad<IO.T, StringMonoid> {
                override val monad = io
                override val type = type
            }
            val law = ObservedMonadLaw(monad) { IO.narrow((it as WriterT<IO.T, *, *>).run).unsafeRun() }
            val f = { x: Int -> WriterT<IO.T, StringMonoid, Int>(io, type, IO { Pair(x + 1, w("f")) }) }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, WriterT.pure(io, type, 1), f, f) }
        }

        context("as MonadWriter") {
            val law = MonadWriterLaw(instance, w(""), { a, b -> w(a.value + b.value) }, observe)
            it("should satisfy laws") { law.assertSatisfyingMonadWriterLaw(1, w("a"), w("b")) }
        }

        context("with primitives") {
            it("should accumulate output with tell") {
                val program = WriterT.tell(identity, type, w("a")) bind { WriterT.tell(identity, type, w("b")) }
                program.run shouldBe Identity(Pair(Unit, w("ab")))
            }

            it("should lift an inner value with empty output") {
                WriterT.lift(maybe, type, Maybe.Just(1)).run shouldBe Maybe.Just(Pair(1, w("")))
            }

            it("should expose output with listen") {
                WriterT.writer(identity, type, Pair(1, w("log"))).listen().run shouldBe Identity(Pair(Pair(1, w("log")), w("log")))
            }

            it("should modify output with censor") {
                WriterT.writer(identity, type, Pair(1, w("log"))).censor { w(it.value.uppercase()) }.run shouldBe Identity(Pair(1, w("LOG")))
            }

            it("should apply a wrapped function and combine output") {
                val result = WriterT.writer(identity, type, Pair(1, w("v"))) ap WriterT.writer(identity, type, Pair({ x: Int -> x + 1 }, w("f")))
                result.run shouldBe Identity(Pair(2, w("fv")))
            }
        }

        context("fx") {
            it("should accumulate output over Maybe") {
                val program = WriterT.fx(maybe, type) {
                    val a = WriterT.writer(maybe, type, Pair(1, w("a"))).bind()
                    WriterT.tell(maybe, type, w("b")).bind()
                    a + 1
                }
                program.run shouldBe Maybe.Just(Pair(2, w("ab")))
            }

            it("should defer IO effects and allow running repeatedly") {
                var count = 0
                val program = WriterT.fx(io, type) {
                    val x = WriterT.lift(io, type, IO { count += 1; count }).bind()
                    WriterT.tell(io, type, w("$x")).bind()
                    x
                }
                count shouldBe 0
                IO.narrow(program.run).unsafeRun() shouldBe Pair(1, w("1"))
                IO.narrow(program.run).unsafeRun() shouldBe Pair(2, w("2"))
            }
        }
    }
})
