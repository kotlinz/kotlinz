package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.law.MonoidLaw
import io.kotest.core.spec.style.DescribeSpec

class AnyMonoidTest : DescribeSpec({
    describe("Any Monoid") {
        val law = MonoidLaw(AnyMonoid::class.java)
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(true.toAnyMonoid(), true.toAnyMonoid(), false.toAnyMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(true.toAnyMonoid(), false.toAnyMonoid(), true.toAnyMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(false.toAnyMonoid(), true.toAnyMonoid(), false.toAnyMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(false.toAnyMonoid(), false.toAnyMonoid(), true.toAnyMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(false.toAnyMonoid(), false.toAnyMonoid(), false.toAnyMonoid()) }
        it("should satisfy laws") { law.assertSatisfyingMonoidLaw(true.toAnyMonoid(), true.toAnyMonoid(), true.toAnyMonoid()) }
    }
})