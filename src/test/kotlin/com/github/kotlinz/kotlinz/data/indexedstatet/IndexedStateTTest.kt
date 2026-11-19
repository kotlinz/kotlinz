package com.github.kotlinz.kotlinz.data.indexedstatet

import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.law.IxMonadLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class IndexedStateTTest : DescribeSpec({
    describe("IndexedStateT") {
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}

        context("as IxMonad over Identity") {
            val law = IxMonadLaw(IndexedStateTIxMonad.of(identity)) { (it as IndexedStateT<Identity.T, Int, *, *>).run(3) }
            val v = IndexedStateT<Identity.T, Int, String, Int>(identity) { i -> Identity(Pair(i * 2, "s$i")) }
            val f = { x: Int -> IndexedStateT<Identity.T, String, Int, Int>(identity) { s -> Identity(Pair(x + s.length, s.length)) } }
            val g = { x: Int -> IndexedStateT<Identity.T, Int, Boolean, String>(identity) { i -> Identity(Pair("$x/$i", i > x)) } }

            it("should satisfy left identity") { law.leftIdentity(1) { x: Int -> IndexedStateT<Identity.T, Int, String, Int>(identity) { i -> Identity(Pair(x + i, "$i")) } } }
            it("should satisfy right identity") { law.rightIdentity(v) }
            it("should satisfy associativity") { law.associativity(v, f, g) }
        }

        context("with primitives") {
            it("should change the state type with put") {
                val program = IndexedStateT.get<Identity.T, Int>(identity) bind { n -> IndexedStateT.put<Identity.T, Int, String>(identity, "n=$n") fmap { n } }
                program.run(1) shouldBe Identity(Pair(1, "n=1"))
            }

            it("should short-circuit through the inner monad") {
                val fail = IndexedStateT<Maybe.T, Int, String, Int>(maybe) { Maybe.None() }
                val program = fail bind { IndexedStateT.modify<Maybe.T, String, Int>(maybe) { it.length } }
                program.run(1) shouldBe Maybe.None<Pair<Unit, Int>>()
            }

            it("should lift an inner value and modify the state type") {
                val program = IndexedStateT.lift<Maybe.T, Int, Int>(maybe, Maybe.Just(5)) bind { x ->
                    IndexedStateT.modify<Maybe.T, Int, String>(maybe) { "$it+$x" } fmap { x }
                }
                program.eval(1) shouldBe Maybe.Just(5)
                program.exec(1) shouldBe Maybe.Just("1+5")
            }

            it("should apply a wrapped function running it first") {
                val f = IndexedStateT<Identity.T, Int, String, (Int) -> Int>(identity) { i -> Identity(Pair({ x: Int -> x + i }, "$i")) }
                val v = IndexedStateT<Identity.T, String, Boolean, Int>(identity) { s -> Identity(Pair(s.length, s.isEmpty())) }
                (v ap f).run(10) shouldBe Identity(Pair(12, false))
            }
        }
    }
})
