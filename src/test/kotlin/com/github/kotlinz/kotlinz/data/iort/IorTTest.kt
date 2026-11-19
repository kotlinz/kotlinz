package com.github.kotlinz.kotlinz.data.iort

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonadIO
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.IdentityToMaybe
import com.github.kotlinz.kotlinz.law.MFunctorLaw
import com.github.kotlinz.kotlinz.law.MonadErrorLaw
import com.github.kotlinz.kotlinz.law.MonadIOLaw
import com.github.kotlinz.kotlinz.law.MonadTransLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.MaybeToListK
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class IorTTest : DescribeSpec({
    describe("IorT") {
        val type = StringMonoid::class.java
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val io = object : IOMonadIO {}
        val w = { s: String -> StringMonoid(s) }
        fun <F> instance(monad: Monad<F>) = object : IorTMonadError<F, StringMonoid>, IorTMonadTrans<F, StringMonoid> {
            override val monad = monad
            override val type = type
        }
        val id = instance(identity)
        val observe = { v: K1<*, *> -> (v as IorT<*, *, *>).run }

        context("as Monad over Identity") {
            val law = ObservedMonadLaw(id, observe)
            val f = { x: Int -> IorT.both(identity, type, w("f"), x + 1) }
            val g = { x: Int -> if (x > 2) IorT.left(identity, type, w("g")) else IorT.right<Identity.T, StringMonoid, Int>(identity, type, x * 2) }

            it("should satisfy laws with Right") { law.assertSatisfyingMonadLaw(1, IorT.right(identity, type, 1), f, g) }
            it("should satisfy laws with Both") { law.assertSatisfyingMonadLaw(2, IorT.both(identity, type, w("m"), 2), f, g) }
            it("should satisfy laws with Left") { law.assertSatisfyingMonadLaw(3, IorT.left(identity, type, w("m")), f, g) }
        }

        context("as Monad over IO") {
            val law = ObservedMonadLaw(instance(io)) { IO.narrow((it as IorT<IO.T, *, *>).run).unsafeRun() }
            val f = { x: Int -> IorT<IO.T, StringMonoid, Int>(io, type, IO { Ior.Both(w("f"), x + 1) }) }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, IorT.pure(io, type, 1), f, f) }
        }

        context("as MonadError") {
            it("should satisfy laws") { MonadErrorLaw(id, observe).assertSatisfyingMonadErrorLaw(w("e"), 1) }
        }

        context("as MonadTrans / MonadIO / MFunctor") {
            fun <F, G> mfunctor(target: Monad<G>) = object : IorTMFunctor<F, G, StringMonoid> {
                override val target = target
                override val type = type
            }

            it("should satisfy MonadTrans laws") {
                MonadTransLaw(maybe, instance(maybe), observe).assertSatisfyingMonadTransLaw(1, Maybe.Just(1)) { x: Int -> if (x > 1) Maybe.None() else Maybe.Just(x + 1) }
            }

            it("should satisfy MonadIO laws") {
                val monadIO = object : IorTMonadIO<IO.T, StringMonoid> {
                    override val monad = io
                    override val type = type
                }
                MonadIOLaw(monadIO) { IO.narrow((it as IorT<IO.T, *, *>).run).unsafeRun() }.assertSatisfyingMonadIOLaw(1, IO { 1 }) { x -> IO { x + 1 } }
            }

            it("should satisfy MFunctor laws") {
                MFunctorLaw.identity(mfunctor<Maybe.T, Maybe.T>(maybe), IorT.both(maybe, type, w("a"), 1), observe)
                MFunctorLaw.composition(
                    mfunctor<Identity.T, Maybe.T>(maybe),
                    mfunctor<Maybe.T, ListK.T>(object : ListKMonad {}),
                    mfunctor<Identity.T, ListK.T>(object : ListKMonad {}),
                    IdentityToMaybe, MaybeToListK, IorT.both(identity, type, w("a"), 1), observe
                )
            }
        }

        context("with primitives") {
            it("should accumulate left values across Both") {
                val program = IorT.both<Identity.T, StringMonoid, Int>(identity, type, w("a;"), 1) bind { x -> IorT.both(identity, type, w("b;"), x + 1) }
                program.run shouldBe Identity(Ior.Both(w("a;b;"), 2))
            }

            it("should stop on a pure Left, keeping earlier warnings") {
                val program = IorT.both<Identity.T, StringMonoid, Int>(identity, type, w("a;"), 1) bind { IorT.left<Identity.T, StringMonoid, Int>(identity, type, w("stop")) } bind { x -> IorT.right(identity, type, x + 1) }
                program.run shouldBe Identity(Ior.Left<StringMonoid, Int>(w("a;stop")))
            }

            it("should apply a wrapped function accumulating warnings") {
                val result = IorT.both(identity, type, w("v"), 2) ap IorT.both(identity, type, w("f"), { x: Int -> x * 10 })
                result.run shouldBe Identity(Ior.Both(w("fv"), 20))
            }

            it("should recover from a Left") {
                IorT.raiseError<Identity.T, StringMonoid, Int>(identity, type, w("e")).handleErrorWith { IorT.pure(identity, type, it.value.length) }.run shouldBe Identity(Ior.Right<StringMonoid, Int>(1))
            }

            it("should lift and hoist") {
                IorT.lift<Identity.T, StringMonoid, Int>(identity, type, Identity(1)).hoist(maybe, IdentityToMaybe).run shouldBe Maybe.Just(Ior.Right<StringMonoid, Int>(1))
            }
        }

        context("fx") {
            it("should accumulate warnings") {
                val program = IorT.fx<Identity.T, StringMonoid, Int>(identity, type) {
                    val a = IorT.both(identity, type, w("a;"), 1).bind()
                    val b = IorT.both(identity, type, w("b;"), 2).bind()
                    a + b
                }
                program.run shouldBe Identity(Ior.Both(w("a;b;"), 3))
            }

            it("should defer IO effects and allow running repeatedly") {
                var count = 0
                val program = IorT.fx<IO.T, StringMonoid, Int>(io, type) {
                    val x = IorT.liftIO<IO.T, StringMonoid, Int>(io, type, IO { count += 1; count }).bind()
                    IorT.both(io, type, w("x=$x"), x).bind()
                }
                count shouldBe 0
                IO.narrow(program.run).unsafeRun() shouldBe Ior.Both(w("x=1"), 1)
                IO.narrow(program.run).unsafeRun() shouldBe Ior.Both(w("x=2"), 2)
            }
        }
    }
})
