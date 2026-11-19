package com.github.kotlinz.kotlinz.data.rwst

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
import com.github.kotlinz.kotlinz.law.MonadReaderLaw
import com.github.kotlinz.kotlinz.law.MonadStateLaw
import com.github.kotlinz.kotlinz.law.MonadTransLaw
import com.github.kotlinz.kotlinz.law.MonadWriterLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.MaybeToListK
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RWSTTest : DescribeSpec({
    describe("RWST") {
        val type = StringMonoid::class.java
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val io = object : IOMonadIO {}
        val w = { s: String -> StringMonoid(s) }
        fun <F> instance(monad: Monad<F>) = object : RWSTMonadReader<F, Int, StringMonoid, Int>, RWSTMonadWriter<F, Int, StringMonoid, Int>,
            RWSTMonadState<F, Int, StringMonoid, Int>, RWSTMonadTrans<F, Int, StringMonoid, Int> {
            override val monad = monad
            override val type = type
        }
        val id = instance(identity)
        val observe = { v: K1<*, *> -> (v as RWST<*, Int, *, Int, *>).run(2, 10) }
        val combine = { a: StringMonoid, b: StringMonoid -> w(a.value + b.value) }

        context("as Monad over Identity") {
            val f = { x: Int -> RWST<Identity.T, Int, StringMonoid, Int, Int>(identity, type) { r, s -> Identity(Triple(x + r, s * 2, w("f"))) } }
            val g = { x: Int -> RWST<Identity.T, Int, StringMonoid, Int, Int>(identity, type) { r, s -> Identity(Triple(x * s, s + r, w("g"))) } }

            it("should satisfy laws") { ObservedMonadLaw(id, observe).assertSatisfyingMonadLaw(1, RWST.get(identity, type), f, g) }
        }

        context("as Monad over IO") {
            val unsafe = { v: K1<*, *> -> IO.narrow((v as RWST<IO.T, Int, *, Int, *>).run(2, 10)).unsafeRun() }
            val f = { x: Int -> RWST<IO.T, Int, StringMonoid, Int, Int>(io, type) { r, s -> IO { Triple(x + r, s + 1, w("f")) } } }

            it("should satisfy laws") { ObservedMonadLaw(instance(io), unsafe).assertSatisfyingMonadLaw(1, RWST.pure(io, type, 1), f, f) }
        }

        context("as MTL") {
            it("should satisfy MonadReader laws") { MonadReaderLaw(id, observe).assertSatisfyingMonadReaderLaw(id.reader { it + 1 }, { it * 2 }, { it - 1 }) }
            it("should satisfy MonadWriter laws") { MonadWriterLaw(id, w(""), combine, observe).assertSatisfyingMonadWriterLaw(1, w("a"), w("b")) }
            it("should satisfy MonadState laws") { MonadStateLaw(id, observe).assertSatisfyingMonadStateLaw(1, 2) { it * 10 } }
        }

        context("as MonadTrans / MonadIO / MFunctor") {
            val f = { x: Int -> if (x > 1) Maybe.None() else Maybe.Just(x + 1) }
            fun <F, G> mfunctor(target: Monad<G>) = object : RWSTMFunctor<F, G, Int, StringMonoid, Int> {
                override val target = target
                override val type = type
            }

            it("should satisfy MonadTrans laws") { MonadTransLaw(maybe, instance(maybe), observe).assertSatisfyingMonadTransLaw(1, Maybe.Just(1), f) }

            it("should satisfy MonadIO laws") {
                val monadIO = object : RWSTMonadIO<IO.T, Int, StringMonoid, Int> {
                    override val monad = io
                    override val type = type
                }
                val unsafe = { v: K1<*, *> -> IO.narrow((v as RWST<IO.T, Int, *, Int, *>).run(2, 10)).unsafeRun() }
                MonadIOLaw(monadIO, unsafe).assertSatisfyingMonadIOLaw(1, IO { 1 }) { x -> IO { x + 1 } }
            }

            it("should satisfy MFunctor laws") {
                MFunctorLaw.identity(mfunctor<Maybe.T, Maybe.T>(maybe), RWST.tell<Maybe.T, Int, StringMonoid, Int>(maybe, type, w("a")), observe)
                MFunctorLaw.composition(
                    mfunctor<Identity.T, Maybe.T>(maybe),
                    mfunctor<Maybe.T, ListK.T>(object : ListKMonad {}),
                    mfunctor<Identity.T, ListK.T>(object : ListKMonad {}),
                    IdentityToMaybe, MaybeToListK, RWST.ask<Identity.T, Int, StringMonoid, Int>(identity, type), observe
                )
            }
        }

        context("with primitives") {
            it("should read, write and update state") {
                val program = RWST.ask<Identity.T, Int, StringMonoid, Int>(identity, type) bind { r ->
                    RWST.tell<Identity.T, Int, StringMonoid, Int>(identity, type, w("r=$r")) bind {
                        RWST.modify<Identity.T, Int, StringMonoid, Int>(identity, type) { it + r } bind { RWST.get(identity, type) }
                    }
                }
                program.run(2, 10) shouldBe Identity(Triple(12, 12, w("r=2")))
                program.eval(2, 10) shouldBe Identity(Pair(12, w("r=2")))
                program.exec(2, 10) shouldBe Identity(Pair(12, w("r=2")))
            }

            it("should modify the environment locally") {
                RWST.ask<Identity.T, Int, StringMonoid, Int>(identity, type).local { it * 5 }.run(2, 0) shouldBe Identity(Triple(10, 0, w("")))
            }

            it("should listen and censor the log") {
                val logged = RWST.writer<Identity.T, Int, StringMonoid, Int, Int>(identity, type, Pair(1, w("a")))
                logged.listen().run(0, 0) shouldBe Identity(Triple(Pair(1, w("a")), 0, w("a")))
                logged.censor { w(it.value.uppercase()) }.run(0, 0) shouldBe Identity(Triple(1, 0, w("A")))
            }

            it("should short-circuit with an inner None") {
                val program = RWST.lift<Maybe.T, Int, StringMonoid, Int, Int>(maybe, type, Maybe.None()) bind { RWST.put(maybe, type, it) }
                program.run(0, 0) shouldBe Maybe.None<Triple<Unit, Int, StringMonoid>>()
            }

            it("should swap the base with hoist") {
                RWST.gets<Identity.T, Int, StringMonoid, Int, Int>(identity, type) { it + 1 }.hoist(maybe, IdentityToMaybe).run(0, 1) shouldBe Maybe.Just(Triple(2, 1, w("")))
            }
        }

        context("fx") {
            it("should combine all three effects") {
                val program = RWST.fx<Identity.T, Int, StringMonoid, Int, Int>(identity, type) {
                    val r = RWST.ask<Identity.T, Int, StringMonoid, Int>(identity, type).bind()
                    RWST.tell<Identity.T, Int, StringMonoid, Int>(identity, type, w("r=$r")).bind()
                    RWST.modify<Identity.T, Int, StringMonoid, Int>(identity, type) { it + r }.bind()
                    r * 10
                }
                program.run(2, 10) shouldBe Identity(Triple(20, 12, w("r=2")))
            }

            it("should defer IO effects and allow running repeatedly") {
                var count = 0
                val program = RWST.fx<IO.T, Int, StringMonoid, Int, Int>(io, type) {
                    val x = RWST.liftIO<IO.T, Int, StringMonoid, Int, Int>(io, type, IO { count += 1; count }).bind()
                    RWST.modify<IO.T, Int, StringMonoid, Int>(io, type) { it + x }.bind()
                    x
                }
                count shouldBe 0
                IO.narrow(program.run(0, 10)).unsafeRun() shouldBe Triple(1, 11, w(""))
                IO.narrow(program.run(0, 10)).unsafeRun() shouldBe Triple(2, 12, w(""))
            }
        }
    }
})
