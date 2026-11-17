package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.law.MonadReaderLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ReaderTTest : DescribeSpec({
    describe("ReaderT") {
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val io = object : IOMonad {}
        val instance = object : ReaderTMonadReader<Identity.T, Int> { override val monad = identity }
        val observe = { v: Any? -> (v as ReaderT<Identity.T, Int, *>).run(3) }

        context("as Monad over Identity") {
            val law = ObservedMonadLaw(instance, observe)
            val f = { x: Int -> ReaderT<Identity.T, Int, Int>(identity) { r -> Identity(x + r) } }
            val g = { x: Int -> ReaderT<Identity.T, Int, Int>(identity) { r -> Identity(x * r) } }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, ReaderT.ask(identity), f, g) }
        }

        context("as Monad over IO") {
            val law = ObservedMonadLaw(object : ReaderTMonad<IO.T, Int> { override val monad = io }) { IO.narrow((it as ReaderT<IO.T, Int, *>).run(3)).unsafeRun() }
            val f = { x: Int -> ReaderT<IO.T, Int, Int>(io) { r -> IO { x + r } } }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, ReaderT.pure(io, 1), f, f) }
        }

        context("as MonadReader") {
            val law = MonadReaderLaw(instance, observe)
            it("should satisfy laws") {
                law.assertSatisfyingMonadReaderLaw(ReaderT<Identity.T, Int, Int>(identity) { r -> Identity(r * 10) }, { it + 1 }, { it * 2 })
            }
        }

        context("with primitives") {
            it("should ask the environment") {
                ReaderT.ask<Identity.T, Int>(identity).run(42) shouldBe Identity(42)
            }

            it("should modify the environment with local") {
                ReaderT.ask<Identity.T, Int>(identity).local { it * 2 }.run(21) shouldBe Identity(42)
            }

            it("should project the environment with reader") {
                ReaderT.reader<Identity.T, String, Int>(identity) { it.length }.run("abc") shouldBe Identity(3)
            }

            it("should lift an inner value ignoring the environment") {
                ReaderT.lift<Maybe.T, Int, Int>(maybe, Maybe.None()).run(1) shouldBe Maybe.None<Int>()
            }

            it("should apply a wrapped function") {
                val result = ReaderT.ask<Identity.T, Int>(identity) ap ReaderT.pure(identity, { x: Int -> x + 1 })
                result.run(1) shouldBe Identity(2)
            }
        }

        context("fx") {
            it("should read the environment over Maybe") {
                val program = ReaderT.fx<Maybe.T, Int, Int>(maybe) {
                    val r = ReaderT.ask<Maybe.T, Int>(maybe).bind()
                    val x = ReaderT.lift<Maybe.T, Int, Int>(maybe, Maybe.Just(2)).bind()
                    r * x
                }
                program.run(21) shouldBe Maybe.Just(42)
            }

            it("should defer IO effects and allow running repeatedly") {
                var count = 0
                val program = ReaderT.fx<IO.T, Int, Int>(io) {
                    val r = ReaderT.ask<IO.T, Int>(io).bind()
                    ReaderT.lift<IO.T, Int, Int>(io, IO { count += 1; count }).bind() * r
                }
                count shouldBe 0
                IO.narrow(program.run(10)).unsafeRun() shouldBe 10
                IO.narrow(program.run(10)).unsafeRun() shouldBe 20
            }
        }
    }
})
