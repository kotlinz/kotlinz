package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MaybeTTest : DescribeSpec({
    describe("MaybeT") {
        val identity = object : IdentityMonad {}
        val io = object : IOMonad {}

        context("as Monad over Identity") {
            val law = ObservedMonadLaw(object : MaybeTMonad<Identity.T> { override val monad = identity }) { (it as MaybeT<*, *>).run }
            val f = { x: Int -> MaybeT.pure(identity, x + 1) }
            val g = { x: Int -> if (x > 2) MaybeT.none(identity) else MaybeT.pure(identity, x * 2) }

            it("should satisfy laws with Just") { law.assertSatisfyingMonadLaw(1, MaybeT.pure(identity, 1), f, g) }
            it("should satisfy laws with None") { law.assertSatisfyingMonadLaw(3, MaybeT.none(identity), f, g) }
        }

        context("as Monad over IO") {
            val law = ObservedMonadLaw(object : MaybeTMonad<IO.T> { override val monad = io }) { (it as MaybeT<IO.T, *>).run.let { r -> IO.narrow(r).unsafeRun() } }
            val f = { x: Int -> MaybeT.lift(io, IO { x + 1 }) }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, MaybeT.pure(io, 1), f, f) }
        }

        context("with primitives") {
            it("should lift an inner value into Just") {
                MaybeT.lift(identity, Identity(1)).run shouldBe Identity(Maybe.Just(1))
            }

            it("should short-circuit on none") {
                val result = MaybeT.none<Identity.T, Int>(identity) bind { MaybeT.pure(identity, it + 1) }
                result.run shouldBe Identity(Maybe.None<Int>())
            }

            it("should apply a wrapped function") {
                (MaybeT.pure(identity, 1) ap MaybeT.pure(identity, { x: Int -> x + 1 })).run shouldBe Identity(Maybe.Just(2))
            }

            it("should fall back with getOrElse") {
                MaybeT.none<Identity.T, Int>(identity).getOrElse(0) shouldBe Identity(0)
            }
        }

        context("fx") {
            it("should bind values over Identity") {
                val result = MaybeT.fx(identity) {
                    val a = MaybeT.lift(identity, Identity(1)).bind()
                    val b = MaybeT.pure(identity, 2).bind()
                    a + b
                }
                result.run shouldBe Identity(Maybe.Just(3))
            }

            it("should short-circuit on None") {
                var reached = false
                val result = MaybeT.fx<Identity.T, Int>(identity) {
                    MaybeT.none<Identity.T, Int>(identity).bind()
                    reached = true
                    1
                }
                result.run shouldBe Identity(Maybe.None<Int>())
                reached shouldBe false
            }

            it("should defer IO effects and allow running repeatedly") {
                var count = 0
                val program = MaybeT.fx(io) { MaybeT.lift(io, IO { count += 1; count }).bind() * 10 }
                count shouldBe 0
                IO.narrow(program.run).unsafeRun() shouldBe Maybe.Just(10)
                IO.narrow(program.run).unsafeRun() shouldBe Maybe.Just(20)
            }
        }
    }
})
