package com.github.kotlinz.kotlinz.data.coyoneda

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private class Box<A>(val value: A) : K1<Box.T, A> { class T }

private object BoxToListK : NaturalTransformation<Box.T, ListK.T> {
    override fun <A> invoke(fa: K1<Box.T, A>): ListK<A> = listKOf((fa as Box<A>).value)
}

class CoyonedaTest : DescribeSpec({
    describe("Coyoneda") {
        val listFunctor = object : ListKFunctor {}
        val list = listKOf(1, 2, 3)
        val coyoneda = Coyoneda.lift(list)

        context("as Functor") {
            val law = FunctorLaw(object : CoyonedaFunctor<ListK.T> {}) { a, b ->
                Coyoneda.narrow(a).lower(listFunctor) == Coyoneda.narrow(b).lower(listFunctor)
            }
            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(coyoneda, { it + 1 }, { it.toString() }) }
        }

        context("isomorphism") {
            it("should round-trip lower after lift") { coyoneda.lower(listFunctor) shouldBe list }

            it("should round-trip lift after lower") {
                val mapped = coyoneda fmap { it * 2 }
                Coyoneda.lift(mapped.lower(listFunctor)).lower(listFunctor) shouldBe mapped.lower(listFunctor)
            }
        }

        context("free functor") {
            it("should map over a type constructor without a Functor instance") {
                val mapped = Coyoneda.lift(Box(20)) fmap { it + 1 } fmap { it * 2 }
                mapped.hoist(BoxToListK).lower(listFunctor) shouldBe listKOf(42)
            }
        }
    }
})
