package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.FoldableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TryFoldableTest : DescribeSpec({
    describe("Try") {
        context("as Foldable") {
            val law = FoldableLaw(object : TryFoldable {})
            val failure = Try.Failure<Int>(RuntimeException("error"))

            it("should satisfy laws with Success") { law.assertSatisfyingFoldableLaw(Try.Success(1)) }
            it("should satisfy laws with Failure") { law.assertSatisfyingFoldableLaw(failure) }

            it("should fold Success") {
                Try.Success(2).foldl(10) { acc, a -> acc - a } shouldBe 8
                Try.Success(2).foldr(10) { a, acc -> a - acc } shouldBe -8
            }

            it("should ignore Failure") {
                failure.foldl(10) { acc, a -> acc - a } shouldBe 10
                failure.foldr(10) { a, acc -> a - acc } shouldBe 10
            }

            it("should foldMap with Monoid") {
                Try.Success(1).foldMap(StringMonoid::class.java) { it.toString().toStringMonoid() } shouldBe "1".toStringMonoid()
                failure.foldMap(StringMonoid::class.java) { it.toString().toStringMonoid() } shouldBe "".toStringMonoid()
            }
        }
    }
})
