package com.github.kotlinz.kotlinz.data.statet

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

class StateTMonadTransTest : DescribeSpec({
    describe("StateT") {
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val listK = object : ListKMonad {}
        val run = { v: K1<*, *> -> (v as StateT<*, Int, *>).run(3) }

        context("as MonadTrans") {
            val law = MonadTransLaw(maybe, object : StateTMonadTrans<Maybe.T, Int> { override val monad = maybe }, run)
            val f = { x: Int -> if (x > 1) Maybe.None() else Maybe.Just(x + 1) }

            it("should satisfy laws with Just") { law.assertSatisfyingMonadTransLaw(1, Maybe.Just(1), f) }
            it("should satisfy laws with None") { law.assertSatisfyingMonadTransLaw(2, Maybe.None(), f) }
        }

        context("as MFunctor") {
            it("should satisfy identity") {
                val instance = object : StateTMFunctor<Maybe.T, Maybe.T, Int> { override val target = maybe }
                MFunctorLaw.identity(instance, StateT.modify<Maybe.T, Int>(maybe) { it + 1 }, run)
            }

            it("should satisfy composition") {
                MFunctorLaw.composition(
                    object : StateTMFunctor<Identity.T, Maybe.T, Int> { override val target = maybe },
                    object : StateTMFunctor<Maybe.T, ListK.T, Int> { override val target = listK },
                    object : StateTMFunctor<Identity.T, ListK.T, Int> { override val target = listK },
                    IdentityToMaybe, MaybeToListK, StateT.state<Identity.T, Int, Int>(identity) { s -> Pair(s, s * 2) }, run
                )
            }

            it("should swap the base monad") {
                val hoisted = StateT.get<Identity.T, Int>(identity).hoist(maybe, IdentityToMaybe)
                hoisted.run(7) shouldBe Maybe.Just(Pair(7, 7))
            }
        }
    }
})
