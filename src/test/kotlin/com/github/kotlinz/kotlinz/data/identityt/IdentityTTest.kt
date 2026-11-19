package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.io.IOMonadIO
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.EitherId
import com.github.kotlinz.kotlinz.law.IdentityToMaybe
import com.github.kotlinz.kotlinz.law.MFunctorLaw
import com.github.kotlinz.kotlinz.law.MonadErrorLaw
import com.github.kotlinz.kotlinz.law.MonadIOLaw
import com.github.kotlinz.kotlinz.law.MonadReaderLaw
import com.github.kotlinz.kotlinz.law.MonadStateLaw
import com.github.kotlinz.kotlinz.law.MonadTransLaw
import com.github.kotlinz.kotlinz.law.MonadWriterLaw
import com.github.kotlinz.kotlinz.law.ObservedMonadLaw
import com.github.kotlinz.kotlinz.law.ReaderId
import com.github.kotlinz.kotlinz.law.StateId
import com.github.kotlinz.kotlinz.law.TransformerBases
import com.github.kotlinz.kotlinz.law.TransformerBases.w
import com.github.kotlinz.kotlinz.law.WriterId
import com.github.kotlinz.kotlinz.type.transformation.MaybeToListK
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class IdentityTTest : DescribeSpec({
    describe("IdentityT") {
        val maybe = object : MaybeMonad {}
        val io = object : IOMonadIO {}
        val listK = object : ListKMonad {}
        val observe = { v: K1<*, *> -> (v as IdentityT<*, *>).run }

        context("as Monad over Maybe") {
            val law = ObservedMonadLaw(object : IdentityTMonad<Maybe.T> { override val monad = maybe }, observe)
            val f = { x: Int -> IdentityT.pure(maybe, x + 1) }
            val g = { x: Int -> if (x > 2) IdentityT.lift(maybe, Maybe.None()) else IdentityT.pure(maybe, x * 2) }

            it("should satisfy laws with Just") { law.assertSatisfyingMonadLaw(1, IdentityT.pure(maybe, 1), f, g) }
            it("should satisfy laws with None") { law.assertSatisfyingMonadLaw(3, IdentityT.lift(maybe, Maybe.None()), f, g) }
        }

        context("as Monad over IO") {
            val law = ObservedMonadLaw(object : IdentityTMonad<IO.T> { override val monad = io }) { IO.narrow((it as IdentityT<IO.T, *>).run).unsafeRun() }
            val f = { x: Int -> IdentityT(io, IO { x + 1 }) }

            it("should satisfy laws") { law.assertSatisfyingMonadLaw(1, IdentityT.pure(io, 1), f, f) }
        }

        context("as MonadTrans / MonadIO / MFunctor") {
            it("should satisfy MonadTrans laws") {
                val trans = object : IdentityTMonadTrans<Maybe.T> { override val monad = maybe }
                MonadTransLaw(maybe, trans, observe).assertSatisfyingMonadTransLaw(1, Maybe.Just(1)) { x: Int -> if (x > 1) Maybe.None() else Maybe.Just(x + 1) }
            }

            it("should satisfy MonadIO laws") {
                val monadIO = object : IdentityTMonadIO<IO.T> { override val monad = io }
                MonadIOLaw(monadIO) { IO.narrow((it as IdentityT<IO.T, *>).run).unsafeRun() }.assertSatisfyingMonadIOLaw(1, IO { 1 }) { x -> IO { x + 1 } }
            }

            it("should satisfy MFunctor laws") {
                MFunctorLaw.identity(object : IdentityTMFunctor<Maybe.T, Maybe.T> { override val target = maybe }, IdentityT.pure(maybe, 1), observe)
                MFunctorLaw.composition(
                    object : IdentityTMFunctor<Identity.T, Maybe.T> { override val target = maybe },
                    object : IdentityTMFunctor<Maybe.T, ListK.T> { override val target = listK },
                    object : IdentityTMFunctor<Identity.T, ListK.T> { override val target = listK },
                    IdentityToMaybe, MaybeToListK, IdentityT.pure(TransformerBases.identity, 1), observe
                )
            }
        }

        context("as lifted MTL") {
            it("should satisfy MonadState laws") {
                val instance = object : IdentityTMonadState<StateId, Int> { override val monad = TransformerBases.stateT }
                MonadStateLaw(instance) { TransformerBases.runState((it as IdentityT<StateId, *>).run) }.assertSatisfyingMonadStateLaw(1, 2) { it * 10 }
            }

            it("should satisfy MonadReader laws") {
                val instance = object : IdentityTMonadReader<ReaderId, Int> { override val monad = TransformerBases.readerT }
                MonadReaderLaw(instance) { TransformerBases.runReader((it as IdentityT<ReaderId, *>).run) }
                    .assertSatisfyingMonadReaderLaw(instance.reader { it + 1 }, { it * 2 }, { it - 1 })
            }

            it("should satisfy MonadWriter laws") {
                val instance = object : IdentityTMonadWriter<WriterId, StringMonoid> { override val monad = TransformerBases.writerT }
                MonadWriterLaw(instance, w(""), TransformerBases::combine) { TransformerBases.runWriter((it as IdentityT<WriterId, *>).run) }
                    .assertSatisfyingMonadWriterLaw(1, w("a"), w("b"))
            }

            it("should satisfy MonadError laws") {
                val instance = object : IdentityTMonadError<EitherId, String> { override val monad = TransformerBases.eitherT }
                MonadErrorLaw(instance) { TransformerBases.runEither((it as IdentityT<EitherId, *>).run) }.assertSatisfyingMonadErrorLaw("e", 1)
            }
        }

        context("with primitives") {
            it("should delegate to the base monad") {
                val program = IdentityT.lift(maybe, Maybe.Just(1)) bind { x -> IdentityT.pure(maybe, x + 1) } fmap { it * 10 }
                program.run shouldBe Maybe.Just(20)
            }

            it("should apply a wrapped function") {
                (IdentityT.pure(maybe, 2) ap IdentityT.pure(maybe, { x: Int -> x + 1 })).run shouldBe Maybe.Just(3)
            }

            it("should swap the base with hoist") {
                IdentityT.pure(TransformerBases.identity, 1).hoist(maybe, IdentityToMaybe).run shouldBe Maybe.Just(1)
            }
        }

        context("fx") {
            it("should short-circuit with the base monad") {
                val program = IdentityT.fx<Maybe.T, Int>(maybe) {
                    val a = IdentityT.pure(maybe, 1).bind()
                    val b = IdentityT.lift<Maybe.T, Int>(maybe, Maybe.None()).bind()
                    a + b
                }
                program.run shouldBe Maybe.None<Int>()
            }

            it("should defer IO effects") {
                var count = 0
                val program = IdentityT.fx<IO.T, Int>(io) { IdentityT.liftIO(io, IO { count += 1; count }).bind() * 10 }
                count shouldBe 0
                IO.narrow(program.run).unsafeRun() shouldBe 10
                IO.narrow(program.run).unsafeRun() shouldBe 20
            }
        }
    }
})
