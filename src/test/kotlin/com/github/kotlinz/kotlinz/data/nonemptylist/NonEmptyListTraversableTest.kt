package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.data.listk.listKOf
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.data.maybe.MaybeApplicative
import com.github.kotlinz.kotlinz.law.TraversableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class NonEmptyListTraversableTest : DescribeSpec({
    describe("NonEmptyList") {
        context("as Traversable") {
            val law = TraversableLaw(object : NonEmptyListTraversable {})
            val maybe = object : MaybeApplicative {}
            val positive = { x: Int -> if (x > 0) Maybe.Just(x) else Maybe.None<Int>() }
            val g = { x: Int -> listKOf(x, x * 2) }

            it("should satisfy laws with multiple elements") { law.assertSatisfyingTraversableLaw(nonEmptyListOf(1, 2, 3), positive, g) }
            it("should satisfy laws with failing element") { law.assertSatisfyingTraversableLaw(nonEmptyListOf(1, -2), positive, g) }
            it("should satisfy laws with single element") { law.assertSatisfyingTraversableLaw(nonEmptyListOf(1), positive, g) }

            it("should traverse when every element succeeds") {
                nonEmptyListOf(1, 2, 3).traverse(maybe, positive) shouldBe Maybe.Just(nonEmptyListOf(1, 2, 3))
            }

            it("should short-circuit when an element fails") {
                nonEmptyListOf(-1, 2).traverse(maybe, positive) shouldBe Maybe.None<NonEmptyList<Int>>()
                nonEmptyListOf(1, -2).traverse(maybe, positive) shouldBe Maybe.None<NonEmptyList<Int>>()
            }

            it("should sequence a list of Maybe") {
                NonEmptyList.sequence(maybe, nonEmptyListOf(Maybe.Just(1), Maybe.Just(2))) shouldBe Maybe.Just(nonEmptyListOf(1, 2))
            }
        }
    }
})
