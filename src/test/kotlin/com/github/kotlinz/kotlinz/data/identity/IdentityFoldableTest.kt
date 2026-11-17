package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.FoldableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class IdentityFoldableTest : DescribeSpec({
    describe("Identity") {
        context("as Foldable") {
            val foldable = object : IdentityFoldable {}

            it("should satisfy laws") { FoldableLaw(foldable).assertSatisfyingFoldableLaw(Identity(1)) }

            it("should fold the value") {
                Identity(2).foldl(10) { acc, a -> acc - a } shouldBe 8
                Identity(2).foldr(10) { a, acc -> a - acc } shouldBe -8
            }

            it("should foldMap with Monoid") {
                Identity(1).foldMap(StringMonoid::class.java) { it.toString().toStringMonoid() } shouldBe "1".toStringMonoid()
            }

            it("should have length 1") { foldable.length(Identity(1)) shouldBe 1 }
        }
    }
})
