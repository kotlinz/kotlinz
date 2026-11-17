package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.FoldableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EitherFoldableTest : DescribeSpec({
    describe("Either") {
        context("as Foldable") {
            val law = FoldableLaw(object : EitherFoldable<String> {})

            it("should satisfy laws with Right") { law.assertSatisfyingFoldableLaw(Either.Right<String, Int>(1)) }
            it("should satisfy laws with Left") { law.assertSatisfyingFoldableLaw(Either.Left<String, Int>("error")) }

            it("should fold Right") {
                Either.Right<String, Int>(2).foldl(10) { acc, a -> acc - a } shouldBe 8
                Either.Right<String, Int>(2).foldr(10) { a, acc -> a - acc } shouldBe -8
            }

            it("should ignore Left") {
                Either.Left<String, Int>("error").foldl(10) { acc, a -> acc - a } shouldBe 10
                Either.Left<String, Int>("error").foldr(10) { a, acc -> a - acc } shouldBe 10
            }

            it("should foldMap with Monoid") {
                Either.Right<String, Int>(1).foldMap(StringMonoid::class.java) { it.toString().toStringMonoid() } shouldBe "1".toStringMonoid()
                Either.Left<String, Int>("e").foldMap(StringMonoid::class.java) { it.toString().toStringMonoid() } shouldBe "".toStringMonoid()
            }
        }
    }
})
