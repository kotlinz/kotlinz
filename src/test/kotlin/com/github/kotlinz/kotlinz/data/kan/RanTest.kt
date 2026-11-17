package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.FunctorLaw
import com.github.kotlinz.kotlinz.type.transformation.MaybeToListK
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val catMaybes = object : FromComposition<ListK.T, Maybe.T, ListK.T> {
    override fun <A> invoke(v: K1<ListK.T, K1<Maybe.T, A>>): ListK<A> =
        ListK(ListK.narrow(v).value.flatMap { MaybeToListK(it).value })
}

private val evenTimesTen = { a: Int -> if (a % 2 == 0) Maybe.Just(a * 10) else Maybe.None() }

@Suppress("UNCHECKED_CAST")
private fun probe(v: K1<*, *>): ListK<Any?> =
    ListK.narrow((v as Ran<Maybe.T, ListK.T, Any?>).runRan { a -> Maybe.Just(a) })

class RanTest : DescribeSpec({
    describe("Ran") {
        val toRanCatMaybes = toRan(object : ListKFunctor {}, catMaybes)
        val ran = Ran.narrow(toRanCatMaybes(listKOf(1, 2, 3, 4)))

        context("as Functor") {
            val law = FunctorLaw(Ran.functor<Maybe.T, ListK.T>()) { a, b -> probe(a) == probe(b) }

            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(ran, { it + 1 }, { it.toString() }) }
        }

        context("operations") {
            it("should run with a continuation into G") { ran.runRan(evenTimesTen) shouldBe listKOf(20, 40) }
            it("should map before running") { (ran fmap { it + 1 }).runRan(evenTimesTen) shouldBe listKOf(20, 40) }
            it("should collapse with gran") { gran(Ran.narrow(toRanCatMaybes(listKOf(Maybe.Just(1), Maybe.None())))) shouldBe listKOf(1) }
        }

        context("universal property") {
            val input = listKOf<K1<Maybe.T, Int>>(Maybe.Just(1), Maybe.None(), Maybe.Just(3))

            it("fromRan should invert toRan") { fromRan(toRanCatMaybes)(input) shouldBe catMaybes(input) }

            it("toRan should invert fromRan") {
                val roundTrip = toRan(object : ListKFunctor {}, fromRan(toRanCatMaybes))
                Ran.narrow(roundTrip(listKOf(1, 2, 3, 4))).runRan(evenTimesTen) shouldBe ran.runRan(evenTimesTen)
            }
        }
    }
})
