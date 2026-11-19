package com.github.kotlinz.kotlinz.data.readert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.law.IdentityToMaybe
import com.github.kotlinz.kotlinz.law.MFunctorLaw
import com.github.kotlinz.kotlinz.law.MonadTransLaw
import com.github.kotlinz.kotlinz.type.transformation.MaybeToListK
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ReaderTMonadTransTest : DescribeSpec({
    describe("ReaderT") {
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val listK = object : ListKMonad {}
        val run = { v: K1<*, *> -> (v as ReaderT<*, Int, *>).run(3) }

        context("as MonadTrans") {
            val law = MonadTransLaw(maybe, object : ReaderTMonadTrans<Maybe.T, Int> { override val monad = maybe }, run)
            val f = { x: Int -> if (x > 1) Maybe.None() else Maybe.Just(x + 1) }

            it("should satisfy laws with Just") { law.assertSatisfyingMonadTransLaw(1, Maybe.Just(1), f) }
            it("should satisfy laws with None") { law.assertSatisfyingMonadTransLaw(2, Maybe.None(), f) }
        }

        context("as MFunctor") {
            it("should satisfy identity") {
                val instance = object : ReaderTMFunctor<Maybe.T, Maybe.T, Int> { override val target = maybe }
                MFunctorLaw.identity(instance, ReaderT.ask<Maybe.T, Int>(maybe), run)
            }

            it("should satisfy composition") {
                MFunctorLaw.composition(
                    object : ReaderTMFunctor<Identity.T, Maybe.T, Int> { override val target = maybe },
                    object : ReaderTMFunctor<Maybe.T, ListK.T, Int> { override val target = listK },
                    object : ReaderTMFunctor<Identity.T, ListK.T, Int> { override val target = listK },
                    IdentityToMaybe, MaybeToListK, ReaderT.reader<Identity.T, Int, Int>(identity) { it * 2 }, run
                )
            }

            it("should swap the base monad") {
                val hoisted = ReaderT.ask<Identity.T, Int>(identity).hoist(maybe, IdentityToMaybe)
                hoisted.run(7) shouldBe Maybe.Just(7)
            }
        }
    }
})
