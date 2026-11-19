package com.github.kotlinz.kotlinz.data.maybet

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

class MaybeTMonadTransTest : DescribeSpec({
    describe("MaybeT") {
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val listK = object : ListKMonad {}
        val run = { v: K1<*, *> -> (v as MaybeT<*, *>).run }

        context("as MonadTrans") {
            val law = MonadTransLaw(maybe, object : MaybeTMonadTrans<Maybe.T> { override val monad = maybe }, run)
            val f = { x: Int -> if (x > 1) Maybe.None() else Maybe.Just(x + 1) }

            it("should satisfy laws with Just") { law.assertSatisfyingMonadTransLaw(1, Maybe.Just(1), f) }
            it("should satisfy laws with None") { law.assertSatisfyingMonadTransLaw(2, Maybe.None(), f) }
        }

        context("as MFunctor") {
            it("should satisfy identity") {
                MFunctorLaw.identity(object : MaybeTMFunctor<Maybe.T, Maybe.T> { override val target = maybe }, MaybeT.none<Maybe.T, Int>(maybe), run)
            }

            it("should satisfy composition") {
                MFunctorLaw.composition(
                    object : MaybeTMFunctor<Identity.T, Maybe.T> { override val target = maybe },
                    object : MaybeTMFunctor<Maybe.T, ListK.T> { override val target = listK },
                    object : MaybeTMFunctor<Identity.T, ListK.T> { override val target = listK },
                    IdentityToMaybe, MaybeToListK, MaybeT.pure(identity, 1), run
                )
            }

            it("should swap the base monad") {
                val hoisted = MaybeT.pure(identity, 1).hoist(maybe, IdentityToMaybe) bind { MaybeT.lift(maybe, Maybe.Just(it + 1)) }
                hoisted.run shouldBe Maybe.Just(Maybe.Just(2))
            }
        }
    }
})
