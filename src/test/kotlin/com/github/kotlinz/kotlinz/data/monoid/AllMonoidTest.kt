package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.law.MonoidLaw
import io.kotest.core.spec.style.DescribeSpec

class AllMonoidTest : DescribeSpec({
    describe("All Monoid") {
        val law = MonoidLaw(AllMonoid::class.java)
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(true.toAllMonoid(), true.toAllMonoid(), false.toAllMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(true.toAllMonoid(), false.toAllMonoid(), true.toAllMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(false.toAllMonoid(), true.toAllMonoid(), false.toAllMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(false.toAllMonoid(), false.toAllMonoid(), true.toAllMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(false.toAllMonoid(), false.toAllMonoid(), false.toAllMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(true.toAllMonoid(), true.toAllMonoid(), true.toAllMonoid()) }
    }
})