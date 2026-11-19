package com.github.kotlinz.kotlinz.data.accumt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonadIO
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.IdentityToMaybe
import com.github.kotlinz.kotlinz.law.MFunctorLaw
import com.github.kotlinz.kotlinz.law.MonadIOLaw
import com.github.kotlinz.kotlinz.law.MonadTransLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.MaybeToListK
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class AccumTTest : DescribeSpec({
    describe("AccumT") {
        val type = StringMonoid::class.java
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val io = object : IOMonadIO {}
        val w = { s: String -> StringMonoid(s) }
        fun <F> instance(monad: Monad<F>) = object : AccumTMonadTrans<F, StringMonoid> {
            override val monad = monad
            override val type = type
        }
        val observe = { v: K1<*, *> -> (v as AccumT<*, StringMonoid, *>).run(w(">")) }

        context("as Monad over Identity") {
            val law = ObservedMonadLaw(instance(identity), observe)
            val f = { x: Int -> AccumT<Identity.T, StringMonoid, Int>(identity, type) { acc -> Identity(Pair(x + acc.value.length, w("f"))) } }
            val g = { x: Int -> AccumT<Identity.T, StringMonoid, Int>(identity, type) { acc -> Identity(Pair(x * acc.value.length, w("g"))) } }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, AccumT.looks(identity, type) { it.value.length }, f, g) }
        }

        context("as Monad over IO") {
            val law = ObservedMonadLaw(instance(io)) { IO.narrow((it as AccumT<IO.T, StringMonoid, *>).run(w(">"))).unsafeRun() }
            val f = { x: Int -> AccumT<IO.T, StringMonoid, Int>(io, type) { acc -> IO { Pair(x + acc.value.length, w("f")) } } }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, AccumT.pure(io, type, 1), f, f) }
        }

        context("as MonadTrans / MonadIO / MFunctor") {
            fun <F, G> mfunctor(target: Monad<G>) = object : AccumTMFunctor<F, G, StringMonoid> {
                override val target = target
                override val type = type
            }

            it("should satisfy MonadTrans laws") {
                MonadTransLaw(maybe, instance(maybe), observe).assertSatisfyingMonadTransLaw(1, Maybe.Just(1)) { x: Int -> if (x > 1) Maybe.None() else Maybe.Just(x + 1) }
            }

            it("should satisfy MonadIO laws") {
                val monadIO = object : AccumTMonadIO<IO.T, StringMonoid> {
                    override val monad = io
                    override val type = type
                }
                MonadIOLaw(monadIO) { IO.narrow((it as AccumT<IO.T, StringMonoid, *>).run(w(""))).unsafeRun() }.assertSatisfyingMonadIOLaw(1, IO { 1 }) { x -> IO { x + 1 } }
            }

            it("should satisfy MFunctor laws") {
                MFunctorLaw.identity(mfunctor<Maybe.T, Maybe.T>(maybe), AccumT.add(maybe, type, w("a")), observe)
                MFunctorLaw.composition(
                    mfunctor<Identity.T, Maybe.T>(maybe),
                    mfunctor<Maybe.T, ListK.T>(object : ListKMonad {}),
                    mfunctor<Identity.T, ListK.T>(object : ListKMonad {}),
                    IdentityToMaybe, MaybeToListK, AccumT.look(identity, type), observe
                )
            }
        }

        context("with primitives") {
            it("should let later steps look at earlier additions") {
                val program = AccumT.add<Identity.T, StringMonoid>(identity, type, w("a")) bind {
                    AccumT.add<Identity.T, StringMonoid>(identity, type, w("b")) bind { AccumT.look(identity, type) }
                }
                program.run(w(">")) shouldBe Identity(Pair(w(">ab"), w("ab")))
                program.eval(w(">")) shouldBe Identity(w(">ab"))
                program.exec(w(">")) shouldBe Identity(w("ab"))
            }

            it("should make the function's output visible to the argument in ap") {
                val result = AccumT.looks(identity, type) { it.value } ap AccumT.accum(identity, type) { acc: StringMonoid -> Pair({ s: String -> "$s|${acc.value}" }, w("f")) }
                result.run(w(">")) shouldBe Identity(Pair(">f|>", w("f")))
            }

            it("should lift without adding output and short-circuit on None") {
                AccumT.lift<Maybe.T, StringMonoid, Int>(maybe, type, Maybe.Just(1)).run(w(">")) shouldBe Maybe.Just(Pair(1, w("")))
                val program = AccumT.lift<Maybe.T, StringMonoid, Int>(maybe, type, Maybe.None()) bind { AccumT.add(maybe, type, w("x")) }
                program.run(w("")) shouldBe Maybe.None<Pair<Unit, StringMonoid>>()
            }

            it("should swap the base with hoist") {
                AccumT.look(identity, type).hoist(maybe, IdentityToMaybe).run(w("a")) shouldBe Maybe.Just(Pair(w("a"), w("")))
            }
        }

        context("fx") {
            it("should accumulate and look") {
                val program = AccumT.fx<Identity.T, StringMonoid, Int>(identity, type) {
                    AccumT.add(identity, type, w("ab")).bind()
                    AccumT.add(identity, type, w("c")).bind()
                    AccumT.looks(identity, type) { it.value.length }.bind()
                }
                program.run(w("")) shouldBe Identity(Pair(3, w("abc")))
            }

            it("should defer IO effects and allow running repeatedly") {
                var count = 0
                val program = AccumT.fx<IO.T, StringMonoid, Int>(io, type) {
                    val x = AccumT.liftIO<IO.T, StringMonoid, Int>(io, type, IO { count += 1; count }).bind()
                    AccumT.add(io, type, w("$x")).bind()
                    x
                }
                count shouldBe 0
                IO.narrow(program.run(w(""))).unsafeRun() shouldBe Pair(1, w("1"))
                IO.narrow(program.run(w(""))).unsafeRun() shouldBe Pair(2, w("2"))
            }
        }
    }
})
