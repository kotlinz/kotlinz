package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.BifunctorLaw
import com.github.kotlinz.kotlinz.law.BitraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class ValidatedBitraversableTest : DescribeSpec({
    describe("Validated") {
        context("as Bifunctor") {
            val law = BifunctorLaw(object : ValidatedBifunctor {})

            it("should satisfy laws with Invalid") { law.assertSatisfyingBifunctorLaw(Validated.invalid<String, Int>("e"), { it + "!" }, { it.uppercase() }, { it + 1 }, { it * 2 }) }
            it("should satisfy laws with Valid") { law.assertSatisfyingBifunctorLaw(Validated.valid<String, Int>(1), { it + "!" }, { it.uppercase() }, { it + 1 }, { it * 2 }) }

            it("should map each side") {
                Validated.invalid<String, Int>("e").bimap({ it + "!" }, { it + 1 }) shouldBe Validated.invalid("e!")
                (Validated.valid<String, Int>(1) first { it.length }) shouldBe Validated.valid(1)
                (Validated.valid<String, Int>(1) second { it + 1 }) shouldBe Validated.valid(2)
            }
        }

        context("as Bitraversable") {
            val law = BitraversableLaw(object : ValidatedBitraversable {})
            val maybe = object : MaybeApplicative {}
            val nonEmpty = { s: String -> if (s.isNotEmpty()) Maybe.Just(s) else Maybe.None<String>() }
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }
            val h = { s: String -> listKOf(s, s + s) }
            val i = { x: Int -> listKOf(x, x * 2) }

            it("should satisfy laws with Invalid") { law.assertSatisfyingBitraversableLaw(Validated.invalid<String, Int>("e"), nonEmpty, positive, h, i) }
            it("should satisfy laws with Valid") { law.assertSatisfyingBitraversableLaw(Validated.valid<String, Int>(1), nonEmpty, positive, h, i) }
            it("should satisfy laws with failing Valid") { law.assertSatisfyingBitraversableLaw(Validated.valid<String, Int>(-1), nonEmpty, positive, h, i) }
            it("should be consistent with bimap") { law.bimapConsistency(Validated.valid<String, Int>(1), { it + "!" }, { it + 1 }).shouldBeTrue() }

            it("should traverse each side") {
                Validated.invalid<String, Int>("e").bitraverse(maybe, nonEmpty, positive) shouldBe Maybe.Just(Validated.invalid<String, Int>("e"))
                Validated.valid<String, Int>(-1).bitraverse(maybe, nonEmpty, positive) shouldBe Maybe.None<Validated<String, Int>>()
            }

            it("should bisequence") {
                Validated.bisequence(maybe, Validated.valid<K1<Maybe.T, String>, K1<Maybe.T, Int>>(Maybe.Just(1))) shouldBe Maybe.Just(Validated.valid<String, Int>(1))
            }
        }
    }
})
