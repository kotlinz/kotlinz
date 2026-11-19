package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.BitraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class EitherBitraversableTest : DescribeSpec({
    describe("Either") {
        context("as Bitraversable") {
            val law = BitraversableLaw(object : EitherBitraversable {})
            val maybe = object : MaybeApplicative {}
            val nonEmpty = { s: String -> if (s.isNotEmpty()) Maybe.Just(s) else Maybe.None<String>() }
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }
            val h = { s: String -> listKOf(s, s + s) }
            val i = { x: Int -> listKOf(x, x * 2) }

            it("should satisfy laws with Left") { law.assertSatisfyingBitraversableLaw(Either.Left<String, Int>("e"), nonEmpty, positive, h, i) }
            it("should satisfy laws with failing Left") { law.assertSatisfyingBitraversableLaw(Either.Left<String, Int>(""), nonEmpty, positive, h, i) }
            it("should satisfy laws with Right") { law.assertSatisfyingBitraversableLaw(Either.Right<String, Int>(1), nonEmpty, positive, h, i) }
            it("should satisfy laws with failing Right") { law.assertSatisfyingBitraversableLaw(Either.Right<String, Int>(-1), nonEmpty, positive, h, i) }
            it("should be consistent with bimap") { law.bimapConsistency(Either.Left<String, Int>("e"), { it + "!" }, { it + 1 }).shouldBeTrue() }

            it("should traverse the Left value with the first function") {
                Either.Left<String, Int>("e").bitraverse(maybe, nonEmpty, positive) shouldBe Maybe.Just(Either.Left<String, Int>("e"))
                Either.Left<String, Int>("").bitraverse(maybe, nonEmpty, positive) shouldBe Maybe.None<Either<String, Int>>()
            }

            it("should traverse the Right value with the second function") {
                Either.Right<String, Int>(1).bitraverse(maybe, nonEmpty, positive) shouldBe Maybe.Just(Either.Right<String, Int>(1))
                Either.Right<String, Int>(-1).bitraverse(maybe, nonEmpty, positive) shouldBe Maybe.None<Either<String, Int>>()
            }

            it("should bisequence") {
                Either.bisequence(maybe, Either.Right<K1<Maybe.T, String>, K1<Maybe.T, Int>>(Maybe.Just(1))) shouldBe Maybe.Just(Either.Right<String, Int>(1))
            }
        }
    }
})
