package com.github.kotlinz.kotlinz.data.semiring

import com.github.kotlinz.kotlinz.law.FieldLaw
import com.github.kotlinz.kotlinz.law.RingLaw
import com.github.kotlinz.kotlinz.type.ring.Field
import com.github.kotlinz.kotlinz.type.ring.Ring
import com.github.kotlinz.kotlinz.type.ring.Semiring
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class RingTest : DescribeSpec({
    describe("Int Ring") {
        val law = RingLaw(IntSemiring::class.java)
        it("should satisfy laws") {
            law.assertSatisfyingRingLaw(2.toSemiring(), (-3).toSemiring(), 5.toSemiring())
            law.assertSatisfyingRingLaw(Int.MAX_VALUE.toSemiring(), Int.MIN_VALUE.toSemiring(), 7.toSemiring())
        }
        it("should negate and subtract") {
            Ring.negate(IntSemiring::class.java, 2.toSemiring()) shouldBe (-2).toSemiring()
            Ring.minus(IntSemiring::class.java, 2.toSemiring(), 3.toSemiring()) shouldBe (-1).toSemiring()
        }
    }

    describe("Long Ring") {
        val law = RingLaw(LongSemiring::class.java)
        it("should satisfy laws") {
            law.assertSatisfyingRingLaw(2L.toSemiring(), (-3L).toSemiring(), 5L.toSemiring())
            law.assertSatisfyingRingLaw(Long.MAX_VALUE.toSemiring(), Long.MIN_VALUE.toSemiring(), 7L.toSemiring())
        }
        it("should subtract") { Ring.minus(LongSemiring::class.java, 5L.toSemiring(), 3L.toSemiring()) shouldBe 2L.toSemiring() }
    }

    describe("Double Field") {
        val clazz = DoubleField::class.java
        val law = FieldLaw(clazz)
        it("should satisfy laws for exactly representable values") {
            law.assertSatisfyingFieldLaw(2.0.toField(), 0.5.toField(), 4.0.toField())
            law.assertSatisfyingFieldLaw(0.25.toField(), (-8.0).toField(), 1.0.toField())
            law.assertSatisfyingFieldLaw(0.0.toField(), 2.0.toField(), 0.5.toField())
        }
        it("should subtract and divide") {
            Ring.minus(clazz, 1.5.toField(), 2.0.toField()) shouldBe (-0.5).toField()
            Field.div(clazz, 3.0.toField(), 2.0.toField()) shouldBe 1.5.toField()
            Field.reciprocal(clazz, 4.0.toField()) shouldBe 0.25.toField()
        }
        it("should follow IEEE 754 caveats") {
            Field.reciprocal(clazz, 0.0.toField()) shouldBe Double.POSITIVE_INFINITY.toField()
            val plus = { a: DoubleField, b: DoubleField -> Semiring.plus(clazz, a, b) }
            plus(plus(0.1.toField(), 0.2.toField()), 0.3.toField()) shouldNotBe plus(0.1.toField(), plus(0.2.toField(), 0.3.toField()))
        }
    }
})
