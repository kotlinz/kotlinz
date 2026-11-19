package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
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

class EitherTMonadTransTest : DescribeSpec({
    describe("EitherT") {
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val listK = object : ListKMonad {}
        val run = { v: K1<*, *> -> (v as EitherT<*, *, *>).run }

        context("as MonadTrans") {
            val law = MonadTransLaw(maybe, object : EitherTMonadTrans<Maybe.T, String> { override val monad = maybe }, run)
            val f = { x: Int -> if (x > 1) Maybe.None() else Maybe.Just(x + 1) }

            it("should satisfy laws with Just") { law.assertSatisfyingMonadTransLaw(1, Maybe.Just(1), f) }
            it("should satisfy laws with None") { law.assertSatisfyingMonadTransLaw(2, Maybe.None(), f) }
        }

        context("as MFunctor") {
            it("should satisfy identity") {
                val instance = object : EitherTMFunctor<Maybe.T, Maybe.T, String> { override val target = maybe }
                MFunctorLaw.identity(instance, EitherT.raiseError<Maybe.T, String, Int>(maybe, "e"), run)
            }

            it("should satisfy composition") {
                MFunctorLaw.composition(
                    object : EitherTMFunctor<Identity.T, Maybe.T, String> { override val target = maybe },
                    object : EitherTMFunctor<Maybe.T, ListK.T, String> { override val target = listK },
                    object : EitherTMFunctor<Identity.T, ListK.T, String> { override val target = listK },
                    IdentityToMaybe, MaybeToListK, EitherT.pure<Identity.T, String, Int>(identity, 1), run
                )
            }

            it("should swap the base monad") {
                val hoisted = EitherT.pure<Identity.T, String, Int>(identity, 1).hoist(maybe, IdentityToMaybe)
                hoisted.run shouldBe Maybe.Just(Either.Right<String, Int>(1))
            }
        }
    }
})
