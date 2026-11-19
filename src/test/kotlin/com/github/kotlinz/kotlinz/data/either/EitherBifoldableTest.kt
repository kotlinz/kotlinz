package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.BifoldableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class EitherBifoldableTest : DescribeSpec({
    describe("Either") {
        context("as Bifoldable") {
            val law = BifoldableLaw(object : EitherBifoldable {})
            val left = Either.Left<String, Int>("error")
            val right = Either.Right<String, Int>(1)

            it("should satisfy laws with Left") { law.assertSatisfyingBifoldableLaw(left) }
            it("should satisfy laws with Right") { law.assertSatisfyingBifoldableLaw(right) }

            it("should fold the Left value with the first function") {
                left.bifoldLeft("", { acc, s -> acc + s }, { acc, i -> acc + i }) shouldBe "error"
                left.bifoldRight(0, { s, acc -> acc + s.length }, { i, acc -> acc + i }) shouldBe 5
            }

            it("should fold the Right value with the second function") {
                right.bifoldLeft("", { acc, s -> acc + s }, { acc, i -> acc + "#$i" }) shouldBe "#1"
                right.bifoldRight(10, { s, acc -> acc + s.length }, { i, acc -> acc + i }) shouldBe 11
            }

            it("should bifoldMap into a monoid") {
                right.bifoldMap(StringMonoid::class.java, { it.toStringMonoid() }, { "#$it".toStringMonoid() }) shouldBe "#1".toStringMonoid()
                left.bifoldMap(StringMonoid::class.java, { it.toStringMonoid() }, { "#$it".toStringMonoid() }) shouldBe "error".toStringMonoid()
            }
        }
    }
})
