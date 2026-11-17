package com.github.kotlinz.kotlinz.data.listk

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.FoldableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ListKFoldableTest : DescribeSpec({
    describe("ListK") {
        context("as Foldable") {
            val foldable = object : ListKFoldable {}
            val law = FoldableLaw(foldable)

            it("should satisfy laws with non-empty list") { law.assertSatisfyingFoldableLaw(listKOf(1, 2, 3)) }
            it("should satisfy laws with empty list") { law.assertSatisfyingFoldableLaw(listKOf<Int>()) }

            it("should fold from the left") {
                listKOf(1, 2, 3).foldl(10) { acc, a -> acc - a } shouldBe 4
            }

            it("should fold from the right") {
                listKOf(1, 2, 3).foldr(10) { a, acc -> a - acc } shouldBe -8
            }

            it("should foldMap in order") {
                listKOf(1, 2, 3).foldMap(StringMonoid::class.java) { it.toString().toStringMonoid() } shouldBe "123".toStringMonoid()
            }

            it("should provide length and isEmpty") {
                foldable.length(listKOf(1, 2, 3)) shouldBe 3
                foldable.isEmpty(listKOf<Int>()) shouldBe true
            }
        }
    }
})
