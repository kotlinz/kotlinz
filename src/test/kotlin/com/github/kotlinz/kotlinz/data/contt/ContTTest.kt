package com.github.kotlinz.kotlinz.data.contt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonadIO
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.law.MonadContLaw
import com.github.kotlinz.kotlinz.law.MonadIOLaw
import com.github.kotlinz.kotlinz.law.MonadTransLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import com.github.kotlinz.kotlinz.type.monad.Monad
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ContTTest : DescribeSpec({
    describe("ContT") {
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val io = object : IOMonadIO {}
        fun <F> instance(monad: Monad<F>) = object : ContTMonadCont<String, F>, ContTMonadTrans<String, F> { override val monad = monad }
        val observe = { v: K1<*, *> -> (v as ContT<String, Any?, Any?>).let { c -> c.run { a -> c.monad.pure("$a") } } }
        val observeIO = { v: K1<*, *> -> IO.narrow(observe(v) as K1<IO.T, String>).unsafeRun() }

        context("as Monad over Identity") {
            val law = ObservedMonadLaw(instance(identity), observe)
            val f = { x: Int -> ContT.pure<String, Identity.T, Int>(identity, x + 1) }
            val g = { x: Int -> ContT<String, Identity.T, Int>(identity) { k -> if (x > 2) Identity("abort") else k(x * 2) } }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, ContT.pure(identity, 1), f, g) }
            it("should satisfy laws when aborting") { law.assertSatisfyingMonadLaw(3, ContT(identity) { k -> k(3) }, f, g) }
        }

        context("as Monad over IO") {
            val law = ObservedMonadLaw(instance(io), observeIO)
            val f = { x: Int -> ContT.lift<String, IO.T, Int>(io, IO { x + 1 }) }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, ContT.pure(io, 1), f, f) }
        }

        context("as MonadCont") {
            val law = MonadContLaw(instance(identity)) { a, b -> observe(a) == observe(b) }

            it("should satisfy laws") { law.assertSatisfyingMonadContLaw(1, ContT.pure(identity, 2)) { x: Int -> ContT.pure(identity, x + 1) } }
        }

        context("as MonadTrans / MonadIO") {
            it("should satisfy MonadTrans laws") {
                MonadTransLaw(maybe, instance(maybe), observe).assertSatisfyingMonadTransLaw(1, Maybe.Just(1)) { x: Int -> if (x > 1) Maybe.None() else Maybe.Just(x + 1) }
            }

            it("should satisfy MonadIO laws") {
                val monadIO = object : ContTMonadIO<String, IO.T> { override val monad = io }
                MonadIOLaw(monadIO, observeIO).assertSatisfyingMonadIOLaw(1, IO { 1 }) { x -> IO { x + 1 } }
            }
        }

        context("with primitives") {
            it("should escape early with callCC") {
                val program = ContT.callCC<Int, Identity.T, Int, Int>(identity) { exit -> exit(10) bind { ContT.pure(identity, it + 100) } }
                program.eval() shouldBe Identity(10)
            }

            it("should continue normally when the escape is unused") {
                val program = ContT.callCC<Int, Identity.T, Int, Int>(identity) { _ -> ContT.pure(identity, 1) } fmap { it + 1 }
                program.eval() shouldBe Identity(2)
            }

            it("should apply a wrapped function") {
                (ContT.pure<Int, Identity.T, Int>(identity, 2) ap ContT.pure(identity, { x: Int -> x * 3 })).eval() shouldBe Identity(6)
            }

            it("should short-circuit with an inner None") {
                val program = ContT.lift<Int, Maybe.T, Int>(maybe, Maybe.None()) bind { ContT.pure(maybe, it + 1) }
                program.eval() shouldBe Maybe.None<Int>()
            }

            it("should capture the continuation up to reset with shift") {
                val inner = ContT.shift<Int, Identity.T, Int>(identity) { k ->
                    ContT.lift(identity, identity.bind({ a: Int -> k(a + 1) }, k(1)))
                } fmap { it * 10 }
                ContT.reset<Int, Int, Identity.T>(identity, inner).fmap { it + 1 }.eval() shouldBe Identity(111)
            }

            it("should discard the continuation when shift does not call it") {
                val inner = ContT.shift<Int, Identity.T, Int>(identity) { _ -> ContT.pure(identity, 42) } fmap { it * 10 }
                ContT.reset<Int, Int, Identity.T>(identity, inner).eval() shouldBe Identity(42)
            }
        }

        context("fx") {
            it("should sequence with early exit") {
                val program = ContT.callCC<Int, Identity.T, Int, Int>(identity) { exit ->
                    ContT.fx(identity) {
                        val a = ContT.pure<Int, Identity.T, Int>(identity, 1).bind()
                        if (a > 0) exit(a * 100).bind()
                        a
                    }
                }
                program.eval() shouldBe Identity(100)
            }

            it("should defer IO effects and allow running repeatedly") {
                var count = 0
                val program = ContT.fx<Int, IO.T, Int>(io) {
                    val x = ContT.liftIO<Int, IO.T, Int>(io, IO { count += 1; count }).bind()
                    x * 10
                }
                count shouldBe 0
                IO.narrow(program.eval()).unsafeRun() shouldBe 10
                IO.narrow(program.eval()).unsafeRun() shouldBe 20
            }
        }
    }
})
