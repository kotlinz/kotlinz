package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.BitraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class Tuple2BitraversableTest : DescribeSpec({
    describe("Tuple2") {
        context("as Bitraversable") {
            val law = BitraversableLaw(object : Tuple2Bitraversable {})
            val maybe = object : MaybeApplicative {}
            val nonEmpty = { s: String -> if (s.isNotEmpty()) Maybe.Just(s) else Maybe.None<String>() }
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }
            val h = { s: String -> listKOf(s, s + s) }
            val i = { x: Int -> listKOf(x, x * 2) }

            it("should satisfy laws") { law.assertSatisfyingBitraversableLaw(Tuple2("a", 1), nonEmpty, positive, h, i) }
            it("should satisfy laws when an effect fails") { law.assertSatisfyingBitraversableLaw(Tuple2("a", -1), nonEmpty, positive, h, i) }
            it("should be consistent with bimap") { law.bimapConsistency(Tuple2("a", 1), { it + "!" }, { it + 1 }).shouldBeTrue() }

            it("should traverse both components") {
                Tuple2("a", 1).bitraverse(maybe, nonEmpty, positive) shouldBe Maybe.Just(Tuple2("a", 1))
                Tuple2("", 1).bitraverse(maybe, nonEmpty, positive) shouldBe Maybe.None<Tuple2<String, Int>>()
            }

            it("should bisequence") {
                Tuple2.bisequence(maybe, Tuple2<K1<Maybe.T, String>, K1<Maybe.T, Int>>(Maybe.Just("a"), Maybe.Just(1))) shouldBe Maybe.Just(Tuple2("a", 1))
            }
        }
    }
})
