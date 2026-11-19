package com.github.kotlinz.kotlinz.data.writert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.identity.Identity
import com.github.kotlinz.kotlinz.data.identity.IdentityMonad
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKMonad
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeMonad
import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.law.IdentityToMaybe
import com.github.kotlinz.kotlinz.law.MFunctorLaw
import com.github.kotlinz.kotlinz.law.MonadTransLaw
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.MaybeToListK
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class WriterTMonadTransTest : DescribeSpec({
    describe("WriterT") {
        val type = StringMonoid::class.java
        val identity = object : IdentityMonad {}
        val maybe = object : MaybeMonad {}
        val listK = object : ListKMonad {}
        val run = { v: K1<*, *> -> (v as WriterT<*, *, *>).run }
        fun <F, G> mfunctor(target: Monad<G>) = object : WriterTMFunctor<F, G, StringMonoid> {
            override val target = target
            override val type = type
        }

        context("as MonadTrans") {
            val trans = object : WriterTMonadTrans<Maybe.T, StringMonoid> {
                override val monad = maybe
                override val type = type
            }
            val law = MonadTransLaw(maybe, trans, run)
            val f = { x: Int -> if (x > 1) Maybe.None() else Maybe.Just(x + 1) }

            it("should satisfy laws with Just") { law.assertSatisfyingMonadTransLaw(1, Maybe.Just(1), f) }
            it("should satisfy laws with None") { law.assertSatisfyingMonadTransLaw(2, Maybe.None(), f) }
        }

        context("as MFunctor") {
            it("should satisfy identity") {
                MFunctorLaw.identity(mfunctor<Maybe.T, Maybe.T>(maybe), WriterT.tell(maybe, type, StringMonoid("a")), run)
            }

            it("should satisfy composition") {
                MFunctorLaw.composition(
                    mfunctor<Identity.T, Maybe.T>(maybe),
                    mfunctor<Maybe.T, ListK.T>(listK),
                    mfunctor<Identity.T, ListK.T>(listK),
                    IdentityToMaybe, MaybeToListK, WriterT.writer(identity, type, Pair(1, StringMonoid("a"))), run
                )
            }

            it("should swap the base monad") {
                val hoisted = WriterT.writer(identity, type, Pair(1, StringMonoid("a"))).hoist(maybe, IdentityToMaybe)
                hoisted.run shouldBe Maybe.Just(Pair(1, StringMonoid("a")))
            }
        }
    }
})
