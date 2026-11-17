package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.law.MonoidLaw
import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ProductMonoidTest : DescribeSpec({
    describe("Product Monoid") {
        val law = MonoidLaw(ProductMonoid::class.java)
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(2.toProductMonoid(), 3.toProductMonoid(), 4.toProductMonoid()) }
        it("should multiply values") { Monoid.op(ProductMonoid::class.java, 2.toProductMonoid(), 3.toProductMonoid()) shouldBe 6.toProductMonoid() }
        it("should have one as identity") { Monoid.mzero(ProductMonoid::class.java) shouldBe 1.toProductMonoid() }
    }

    describe("Long Product Monoid") {
        val law = MonoidLaw(LongProductMonoid::class.java)
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(2L.toProductMonoid(), 3L.toProductMonoid(), 4L.toProductMonoid()) }
        it("should multiply values") { Monoid.op(LongProductMonoid::class.java, 2L.toProductMonoid(), 3L.toProductMonoid()) shouldBe 6L.toProductMonoid() }
        it("should have one as identity") { Monoid.mzero(LongProductMonoid::class.java) shouldBe 1L.toProductMonoid() }
    }
})
