package com.github.kotlinz.kotlinz.type.transformation

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.either.EitherFunctor
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKFunctor
import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeFunctor
import com.github.kotlinz.kotlinz.law.NaturalityLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun <A> Maybe<A>.toList(): List<A> = MaybeToListK(this).value

private val maybeEq = { a: K1<Maybe.T, *>, b: K1<Maybe.T, *> -> Maybe.narrow(a).toList() == Maybe.narrow(b).toList() }

class NaturalTransformationTest : DescribeSpec({
    describe("NaturalTransformation") {
        val f = { x: Int -> x.toString() }

        context("MaybeToListK") {
            val law = NaturalityLaw(MaybeToListK, object : MaybeFunctor {}, object : ListKFunctor {})

            it("should satisfy naturality with Just") { law.assertSatisfyingNaturalityLaw(Maybe.Just(1), f) }
            it("should satisfy naturality with None") { law.assertSatisfyingNaturalityLaw(Maybe.None<Int>(), f) }
            it("should convert Just to singleton list") { MaybeToListK(Maybe.Just(1)) shouldBe listKOf(1) }
        }

        context("ListKToMaybe") {
            val law = NaturalityLaw(ListKToMaybe, object : ListKFunctor {}, object : MaybeFunctor {}, maybeEq)

            it("should satisfy naturality with non-empty list") { law.assertSatisfyingNaturalityLaw(listKOf(1, 2), f) }
            it("should satisfy naturality with empty list") { law.assertSatisfyingNaturalityLaw(listKOf<Int>(), f) }
        }

        context("EitherToMaybe") {
            val law = NaturalityLaw(EitherToMaybe<String>(), object : EitherFunctor<String> {}, object : MaybeFunctor {}, maybeEq)

            it("should satisfy naturality with Right") { law.assertSatisfyingNaturalityLaw(Either.Right<String, Int>(1), f) }
            it("should satisfy naturality with Left") { law.assertSatisfyingNaturalityLaw(Either.Left<String, Int>("e"), f) }
        }

        context("composition") {
            val roundTrip = MaybeToListK andThen ListKToMaybe

            it("should apply transformations in order") {
                (ListKToMaybe compose MaybeToListK)(Maybe.Just(1)).let { Maybe.narrow(it).toList() } shouldBe listOf(1)
                (roundTrip andThen MaybeToListK)(Maybe.Just(2)) shouldBe listKOf(2)
            }

            it("should treat identity as unit") {
                (NaturalTransformation.identity<ListK.T>() andThen ListKToMaybe andThen MaybeToListK)(listKOf(3, 4)) shouldBe listKOf(3)
                (MaybeToListK compose NaturalTransformation.identity())(Maybe.None<Int>()) shouldBe listKOf()
            }

            it("should satisfy naturality when composed") {
                val law = NaturalityLaw(EitherToMaybe<String>() andThen MaybeToListK, object : EitherFunctor<String> {}, object : ListKFunctor {})
                law.assertSatisfyingNaturalityLaw(Either.Right<String, Int>(5), f)
            }
        }
    }
})
