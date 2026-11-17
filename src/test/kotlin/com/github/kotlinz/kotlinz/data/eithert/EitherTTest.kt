package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad
import com.github.kotlinz.kotlinz.law.MonadErrorLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EitherTTest : DescribeSpec({
    describe("EitherT") {
        val identity = object : IdentityMonad {}
        val io = object : IOMonad {}
        val instance = object : EitherTMonadError<Identity.T, String> { override val monad = identity }
        val observe = { v: Any? -> (v as EitherT<*, *, *>).run }

        context("as Monad over Identity") {
            val law = ObservedMonadLaw(instance, observe)
            val f = { x: Int -> EitherT.pure<Identity.T, String, Int>(identity, x + 1) }
            val g = { x: Int -> if (x > 2) EitherT.raiseError(identity, "too big") else EitherT.pure<Identity.T, String, Int>(identity, x * 2) }

            it("should satisfy laws with Right") { law.assertSatisfyingMonadLaw(1, EitherT.pure(identity, 1), f, g) }
            it("should satisfy laws with Left") { law.assertSatisfyingMonadLaw(3, EitherT.raiseError(identity, "error"), f, g) }
        }

        context("as Monad over IO") {
            val law = ObservedMonadLaw(object : EitherTMonad<IO.T, String> { override val monad = io }) { IO.narrow((it as EitherT<IO.T, *, *>).run).unsafeRun() }
            val f = { x: Int -> EitherT.lift<IO.T, String, Int>(io, IO { x + 1 }) }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, EitherT.pure(io, 1), f, f) }
        }

        context("as MonadError") {
            it("should satisfy laws") { MonadErrorLaw(instance, observe).assertSatisfyingMonadErrorLaw("error", 1) }
        }

        context("with primitives") {
            it("should lift an inner value into Right") {
                EitherT.lift<Identity.T, String, Int>(identity, Identity(1)).run shouldBe Identity(Either.Right<String, Int>(1))
            }

            it("should short-circuit on Left") {
                val result = EitherT.raiseError<Identity.T, String, Int>(identity, "error") bind { EitherT.pure(identity, it + 1) }
                result.run shouldBe Identity(Either.Left<String, Int>("error"))
            }

            it("should recover with handleErrorWith") {
                val result = EitherT.raiseError<Identity.T, String, Int>(identity, "error") handleErrorWith { EitherT.pure(identity, it.length) }
                result.run shouldBe Identity(Either.Right<String, Int>(5))
            }

            it("should apply a wrapped function") {
                val result = EitherT.pure<Identity.T, String, Int>(identity, 1) ap EitherT.pure(identity, { x: Int -> x + 1 })
                result.run shouldBe Identity(Either.Right<String, Int>(2))
            }
        }

        context("fx") {
            it("should short-circuit on Left") {
                var reached = false
                val result = EitherT.fx<Identity.T, String, Int>(identity) {
                    val a = EitherT.fromEither(identity, Either.Right<String, Int>(1)).bind()
                    EitherT.raiseError<Identity.T, String, Int>(identity, "error").bind()
                    reached = true
                    a
                }
                result.run shouldBe Identity(Either.Left<String, Int>("error"))
                reached shouldBe false
            }

            it("should defer IO effects and allow running repeatedly") {
                var count = 0
                val program = EitherT.fx<IO.T, String, Int>(io) { EitherT.lift<IO.T, String, Int>(io, IO { count += 1; count }).bind() * 10 }
                count shouldBe 0
                IO.narrow(program.run).unsafeRun() shouldBe Either.Right<String, Int>(10)
                IO.narrow(program.run).unsafeRun() shouldBe Either.Right<String, Int>(20)
            }
        }
    }
})
