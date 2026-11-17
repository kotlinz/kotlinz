package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.law.MonadStateLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class StateTTest : DescribeSpec({
    describe("StateT") {
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val io = object : IOMonad {}
        val instance = object : StateTMonadState<Identity.T, Int> { override val monad = identity }
        val observe = { v: Any? -> (v as StateT<Identity.T, Int, *>).run(3) }

        context("as Monad over Identity") {
            val law = ObservedMonadLaw(instance, observe)
            val f = { x: Int -> StateT<Identity.T, Int, Int>(identity) { s -> Identity(Pair(x + s, s * 2)) } }
            val g = { x: Int -> StateT<Identity.T, Int, Int>(identity) { s -> Identity(Pair(x * s, s + 1)) } }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, StateT.get(identity), f, g) }
        }

        context("as Monad over IO") {
            val law = ObservedMonadLaw(object : StateTMonad<IO.T, Int> { override val monad = io }) { IO.narrow((it as StateT<IO.T, Int, *>).run(3)).unsafeRun() }
            val f = { x: Int -> StateT<IO.T, Int, Int>(io) { s -> IO { Pair(x + s, s + 1) } } }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, StateT.pure(io, 1), f, f) }
        }

        context("as MonadState") {
            it("should satisfy laws") { MonadStateLaw(instance, observe).assertSatisfyingMonadStateLaw(1, 2) { it * 10 } }
        }

        context("with primitives") {
            it("should thread state through get and put") {
                val program = StateT.get<Identity.T, Int>(identity) bind { n -> StateT.put<Identity.T, Int>(identity, n + 1) fmap { n * 10 } }
                program.run(1) shouldBe Identity(Pair(10, 2))
            }

            it("should modify the state") {
                StateT.modify<Identity.T, Int>(identity) { it * 3 }.exec(2) shouldBe Identity(6)
            }

            it("should project the state with gets") {
                StateT.gets<Identity.T, String, Int>(identity) { it.length }.eval("abc") shouldBe Identity(3)
            }

            it("should lift an inner value keeping the state") {
                StateT.lift<Maybe.T, Int, String>(maybe, Maybe.Just("a")).run(5) shouldBe Maybe.Just(Pair("a", 5))
            }

            it("should short-circuit with an inner None") {
                val program = StateT.lift<Maybe.T, Int, Int>(maybe, Maybe.None()) bind { StateT.put<Maybe.T, Int>(maybe, it) }
                program.run(5) shouldBe Maybe.None<Pair<Unit, Int>>()
            }

            it("should apply a wrapped function") {
                val result = StateT.get<Identity.T, Int>(identity) ap StateT.state(identity) { s: Int -> Pair({ x: Int -> x + s }, s * 2) }
                result.run(3) shouldBe Identity(Pair(9, 6))
            }
        }

        context("fx") {
            it("should thread State over Maybe") {
                val program = StateT.fx<Maybe.T, Int, Int>(maybe) {
                    val n = StateT.get<Maybe.T, Int>(maybe).bind()
                    StateT.put<Maybe.T, Int>(maybe, n + 1).bind()
                    n * 10
                }
                program.run(1) shouldBe Maybe.Just(Pair(10, 2))
                program.run(5) shouldBe Maybe.Just(Pair(50, 6))
            }

            it("should defer IO effects and allow running repeatedly") {
                var count = 0
                val program = StateT.fx<IO.T, Int, Int>(io) {
                    val x = StateT.lift<IO.T, Int, Int>(io, IO { count += 1; count }).bind()
                    StateT.modify<IO.T, Int>(io) { it + x }.bind()
                    x
                }
                count shouldBe 0
                IO.narrow(program.run(10)).unsafeRun() shouldBe Pair(1, 11)
                IO.narrow(program.run(10)).unsafeRun() shouldBe Pair(2, 12)
            }
        }
    }
})
