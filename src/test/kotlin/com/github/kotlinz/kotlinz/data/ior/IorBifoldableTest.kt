package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.BifoldableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class IorBifoldableTest : DescribeSpec({
    describe("Ior") {
        context("as Bifoldable") {
            val law = BifoldableLaw(object : IorBifoldable {})
            val left = Ior.left<String, Int>("w")
            val right = Ior.right<String, Int>(1)
            val both = Ior.both("w", 1)

            it("should satisfy laws with Left") { law.assertSatisfyingBifoldableLaw(left) }
            it("should satisfy laws with Right") { law.assertSatisfyingBifoldableLaw(right) }
            it("should satisfy laws with Both") { law.assertSatisfyingBifoldableLaw(both) }

            it("should fold the left value before the right value") {
                both.bifoldLeft("", { acc, s -> acc + s }, { acc, i -> acc + i }) shouldBe "w1"
                both.bifoldRight("", { s, acc -> s + acc }, { i, acc -> "$i$acc" }) shouldBe "w1"
            }

            it("should fold a single side") {
                left.bifoldLeft("", { acc, s -> acc + s }, { acc, i -> acc + i }) shouldBe "w"
                right.bifoldRight("", { s, acc -> s + acc }, { i, acc -> "$i$acc" }) shouldBe "1"
            }

            it("should bifoldMap into a monoid") {
                both.bifoldMap(StringMonoid::class.java, { it.toStringMonoid() }, { "#$it".toStringMonoid() }) shouldBe "w#1".toStringMonoid()
            }
        }
    }
})
