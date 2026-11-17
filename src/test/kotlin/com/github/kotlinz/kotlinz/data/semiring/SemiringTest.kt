package com.github.kotlinz.kotlinz.data.semiring

import com.github.kotlinz.kotlinz.law.SemiringLaw
import com.github.kotlinz.kotlinz.type.ring.Semiring
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SemiringTest : DescribeSpec({
    describe("Int Semiring") {
        val law = SemiringLaw(IntSemiring::class.java)
        it("should satisfy laws") { law.assertSatisfyingSemiringLaw(2.toSemiring(), (-3).toSemiring(), 5.toSemiring()) }
        it("should add and multiply") {
            Semiring.plus(IntSemiring::class.java, 2.toSemiring(), 3.toSemiring()) shouldBe 5.toSemiring()
            Semiring.times(IntSemiring::class.java, 2.toSemiring(), 3.toSemiring()) shouldBe 6.toSemiring()
        }
    }

    describe("Long Semiring") {
        val law = SemiringLaw(LongSemiring::class.java)
        it("should satisfy laws") { law.assertSatisfyingSemiringLaw(2L.toSemiring(), (-3L).toSemiring(), 5L.toSemiring()) }
    }

    describe("Boolean Semiring") {
        val law = SemiringLaw(BooleanSemiring::class.java)
        it("should satisfy laws") {
            law.assertSatisfyingSemiringLaw(true.toSemiring(), false.toSemiring(), true.toSemiring())
            law.assertSatisfyingSemiringLaw(false.toSemiring(), true.toSemiring(), false.toSemiring())
        }
        it("should use or and and") {
            Semiring.plus(BooleanSemiring::class.java, true.toSemiring(), false.toSemiring()) shouldBe true.toSemiring()
            Semiring.times(BooleanSemiring::class.java, true.toSemiring(), false.toSemiring()) shouldBe false.toSemiring()
        }
    }
})
