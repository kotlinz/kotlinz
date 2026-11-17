package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.FoldableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MaybeFoldableTest : DescribeSpec({
    describe("Maybe") {
        context("as Foldable") {
            val foldable = object : MaybeFoldable {}
            val law = FoldableLaw(foldable)

            it("should satisfy laws with Just") { law.assertSatisfyingFoldableLaw(Maybe.Just(1)) }
            it("should satisfy laws with None") { law.assertSatisfyingFoldableLaw(Maybe.None<Int>()) }

            it("should fold Just") {
                Maybe.Just(2).foldl(10) { acc, a -> acc - a } shouldBe 8
                Maybe.Just(2).foldr(10) { a, acc -> a - acc } shouldBe -8
            }

            it("should return initial for None") {
                Maybe.None<Int>().foldl(10) { acc, a -> acc - a } shouldBe 10
                Maybe.None<Int>().foldr(10) { a, acc -> a - acc } shouldBe 10
            }

            it("should foldMap with Monoid") {
                Maybe.Just(1).foldMap(StringMonoid::class.java) { it.toString().toStringMonoid() } shouldBe "1".toStringMonoid()
                Maybe.None<Int>().foldMap(StringMonoid::class.java) { it.toString().toStringMonoid() } shouldBe "".toStringMonoid()
            }

            it("should provide length and isEmpty") {
                foldable.length(Maybe.Just(1)) shouldBe 1
                foldable.isEmpty(Maybe.None<Int>()) shouldBe true
            }
        }
    }
})
