package com.github.kotlinz.kotlinz.data.nonemptylist

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.FoldableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class NonEmptyListFoldableTest : DescribeSpec({
    describe("NonEmptyList") {
        context("as Foldable") {
            val foldable = object : NonEmptyListFoldable {}
            val law = FoldableLaw(foldable)

            it("should satisfy laws with multiple elements") { law.assertSatisfyingFoldableLaw(nonEmptyListOf(1, 2, 3)) }
            it("should satisfy laws with single element") { law.assertSatisfyingFoldableLaw(nonEmptyListOf(1)) }

            it("should fold from the left") {
                nonEmptyListOf(1, 2, 3).foldl(10) { acc, a -> acc - a } shouldBe 4
            }

            it("should fold from the right") {
                nonEmptyListOf(1, 2, 3).foldr(10) { a, acc -> a - acc } shouldBe -8
            }

            it("should foldMap in order") {
                nonEmptyListOf(1, 2, 3).foldMap(StringMonoid::class.java) { it.toString().toStringMonoid() } shouldBe "123".toStringMonoid()
            }

            it("should never be empty") {
                foldable.isEmpty(nonEmptyListOf(1)) shouldBe false
                foldable.length(nonEmptyListOf(1, 2)) shouldBe 2
            }
        }
    }
})
