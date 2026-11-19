package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKApplicative
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.BitraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class IorBitraversableTest : DescribeSpec({
    describe("Ior") {
        context("as Bitraversable") {
            val law = BitraversableLaw(object : IorBitraversable {})
            val maybe = object : MaybeApplicative {}
            val listK = object : ListKApplicative {}
            val nonEmpty = { s: String -> if (s.isNotEmpty()) Maybe.Just(s) else Maybe.None<String>() }
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }
            val h = { s: String -> listKOf(s, s + s) }
            val i = { x: Int -> listKOf(x, x * 2) }

            it("should satisfy laws with Left") { law.assertSatisfyingBitraversableLaw(Ior.left<String, Int>("w"), nonEmpty, positive, h, i) }
            it("should satisfy laws with Right") { law.assertSatisfyingBitraversableLaw(Ior.right<String, Int>(1), nonEmpty, positive, h, i) }
            it("should satisfy laws with Both") { law.assertSatisfyingBitraversableLaw(Ior.both("w", 1), nonEmpty, positive, h, i) }
            it("should satisfy laws with failing Both") { law.assertSatisfyingBitraversableLaw(Ior.both("", 1), nonEmpty, positive, h, i) }
            it("should be consistent with bimap") { law.bimapConsistency(Ior.both("w", 1), { it + "!" }, { it + 1 }).shouldBeTrue() }

            it("should combine both effects for Both") {
                Ior.both("w", 1).bitraverse(maybe, nonEmpty, positive) shouldBe Maybe.Just(Ior.both("w", 1))
                Ior.both("w", -1).bitraverse(maybe, nonEmpty, positive) shouldBe Maybe.None<Ior<String, Int>>()
                ListK.narrow(Ior.both("w", 1).bitraverse(listK, h, i)).value shouldBe
                        listOf(Ior.both("w", 1), Ior.both("w", 2), Ior.both("ww", 1), Ior.both("ww", 2))
            }

            it("should bisequence") {
                Ior.bisequence(maybe, Ior.both<K1<Maybe.T, String>, K1<Maybe.T, Int>>(Maybe.Just("w"), Maybe.Just(1))) shouldBe Maybe.Just(Ior.both("w", 1))
            }
        }
    }
})
