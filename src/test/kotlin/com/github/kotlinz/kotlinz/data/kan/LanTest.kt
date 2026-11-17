package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.law.FunctorLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private val wrapJust = object : IntoComposition<ListK.T, ListK.T, Maybe.T> {
    override fun <A> invoke(v: K1<ListK.T, A>): ListK<K1<Maybe.T, A>> = ListK(ListK.narrow(v).value.map { Maybe.Just(it) })
}

private val toLanWrapJust = toLan(object : ListKFunctor {}, wrapJust)

private fun lower(v: K1<*, *>): ListK<Any?> {
    @Suppress("UNCHECKED_CAST")
    val lan = v as Lan<Maybe.T, ListK.T, Any?>
    return ListK.narrow(toLanWrapJust(lan))
}

class LanTest : DescribeSpec({
    describe("Lan") {
        val lan = Lan({ m: K1<Maybe.T, Int> -> Maybe.narrow(m).getOrElse(0) * 2 }, listKOf(1, 2, 3))

        context("as Functor") {
            val law = FunctorLaw(Lan.functor<Maybe.T, ListK.T>()) { a, b -> lower(a) == lower(b) }

            it("should satisfy laws") { law.assertSatisfyingFunctorLaw(lan, { it + 1 }, { it.toString() }) }
        }

        context("operations") {
            it("should lower through toLan") { lower(lan) shouldBe listKOf(2, 4, 6) }
            it("should map the result") { lower(lan fmap { it + 1 }) shouldBe listKOf(3, 5, 7) }

            it("should fold with access to the hidden value") {
                val size = lan.fold(object : Lan.Fold<Maybe.T, ListK.T, Int, Int> {
                    override fun <B> invoke(f: (K1<Maybe.T, B>) -> Int, v: K1<ListK.T, B>): Int = ListK.narrow(v).value.size
                })
                size shouldBe 3
            }
        }

        context("universal property") {
            val input = listKOf(1, 2)

            it("glan should embed into Lan") { lower(glan<Maybe.T, ListK.T, Int>(input)) shouldBe listKOf(Maybe.Just(1), Maybe.Just(2)) }
            it("fromLan should invert toLan") { fromLan(toLanWrapJust)(input) shouldBe wrapJust(input) }

            it("toLan should invert fromLan") {
                val roundTrip = toLan(object : ListKFunctor {}, fromLan(toLanWrapJust))
                roundTrip(lan) shouldBe toLanWrapJust(lan)
            }
        }
    }
})
