package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.law.GroupLaw
import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class XorMonoidTest : DescribeSpec({
    describe("Xor Monoid") {
        val law = GroupLaw(XorMonoid::class.java)
        it("should satisfy group laws") { law.assertSatisfyingGroupLaw(true.toXorMonoid(), false.toXorMonoid(), true.toXorMonoid()) }
        it("should satisfy group laws for false") { law.assertSatisfyingGroupLaw(false.toXorMonoid(), true.toXorMonoid(), false.toXorMonoid()) }
        it("should xor values") { Monoid.op(XorMonoid::class.java, true.toXorMonoid(), true.toXorMonoid()) shouldBe false.toXorMonoid() }
    }
})
