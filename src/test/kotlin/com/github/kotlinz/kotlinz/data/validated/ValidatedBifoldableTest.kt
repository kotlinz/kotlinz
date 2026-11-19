package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.data.monoid.StringMonoid
import com.github.kotlinz.kotlinz.data.monoid.toStringMonoid
import com.github.kotlinz.kotlinz.law.BifoldableLaw
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ValidatedBifoldableTest : DescribeSpec({
    describe("Validated") {
        context("as Bifoldable") {
            val law = BifoldableLaw(object : ValidatedBifoldable {})
            val invalid = Validated.invalid<String, Int>("error")
            val valid = Validated.valid<String, Int>(1)

            it("should satisfy laws with Invalid") { law.assertSatisfyingBifoldableLaw(invalid) }
            it("should satisfy laws with Valid") { law.assertSatisfyingBifoldableLaw(valid) }

            it("should fold the error with the first function") {
                invalid.bifoldLeft("", { acc, e -> acc + e }, { acc, i -> acc + i }) shouldBe "error"
            }

            it("should fold the valid value with the second function") {
                valid.bifoldRight(10, { e, acc -> acc + e.length }, { i, acc -> acc + i }) shouldBe 11
            }

            it("should bifoldMap into a monoid") {
                valid.bifoldMap(StringMonoid::class.java, { it.toStringMonoid() }, { "#$it".toStringMonoid() }) shouldBe "#1".toStringMonoid()
            }
        }
    }
})
