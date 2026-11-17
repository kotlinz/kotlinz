package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.law.MonoidLaw
import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SumMonoidTest : DescribeSpec({
    describe("Sum Monoid") {
        val law = MonoidLaw(SumMonoid::class.java)
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(1.toSumMonoid(), 2.toSumMonoid(), 3.toSumMonoid()) }
        it("should add values") { Monoid.op(SumMonoid::class.java, 2.toSumMonoid(), 3.toSumMonoid()) shouldBe 5.toSumMonoid() }
        it("should have zero as identity") { Monoid.mzero(SumMonoid::class.java) shouldBe 0.toSumMonoid() }
    }

    describe("Long Sum Monoid") {
        val law = MonoidLaw(LongSumMonoid::class.java)
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(1L.toSumMonoid(), 2L.toSumMonoid(), 3L.toSumMonoid()) }
        it("should add values") { Monoid.op(LongSumMonoid::class.java, 2L.toSumMonoid(), 3L.toSumMonoid()) shouldBe 5L.toSumMonoid() }
        it("should have zero as identity") { Monoid.mzero(LongSumMonoid::class.java) shouldBe 0L.toSumMonoid() }
    }
})
