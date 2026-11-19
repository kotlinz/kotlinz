package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.listk.ListK
import com.github.kotlinz.kotlinz.data.listk.ListKApplicative
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyList
import com.github.kotlinz.kotlinz.data.nonemptylist.NonEmptyListApplicative
import com.github.kotlinz.kotlinz.law.ApplyLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

private fun <T> zipWith(apply: Apply<T>, a: K1<T, Int>, b: K1<T, Int>): K1<T, Int> =
    apply.ap(apply.fmap({ x: Int -> { y: Int -> x + y } }, a), b)

class ApplyTest : DescribeSpec({
    describe("Apply") {
        it("should be a supertype of Applicative usable without pure") {
            val apply: Apply<Maybe.T> = object : MaybeApplicative {}
            zipWith(apply, Maybe.Just(1), Maybe.Just(2)) shouldBe Maybe.Just(3)
            zipWith(apply, Maybe.Just(1), Maybe.None()) shouldBe Maybe.None<Int>()
        }

        it("should satisfy the associative composition law for Maybe") {
            ApplyLaw(object : MaybeApplicative {}).assertSatisfyingApplyLaw(
                Maybe.Just { n: Int -> n * 2 }, Maybe.Just { n: Int -> n + 1 }, Maybe.Just(3))
        }

        it("should satisfy the associative composition law for ListK") {
            ApplyLaw(object : ListKApplicative {}).assertSatisfyingApplyLaw(
                ListK(listOf({ n: Int -> n * 2 }, { n: Int -> n - 1 })), ListK(listOf({ n: Int -> n + 1 })), ListK(listOf(1, 2)))
        }

        it("should satisfy the associative composition law for NonEmptyList") {
            ApplyLaw(object : NonEmptyListApplicative {}).assertSatisfyingApplyLaw(
                NonEmptyList({ n: Int -> n * 2 }, listOf()), NonEmptyList({ n: Int -> n + 1 }, listOf { n: Int -> n }), NonEmptyList(1, listOf(2)))
        }
    }
})
