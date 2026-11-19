package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.law.BandLaw
import com.github.kotlinz.kotlinz.law.CommutativeMonoidLaw
import com.github.kotlinz.kotlinz.type.group.CommutativeMonoid
import com.github.kotlinz.kotlinz.type.group.Monoid
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CommutativeMonoidTest : DescribeSpec({
    describe("numeric commutative monoids") {
        it("should satisfy laws") {
            CommutativeMonoidLaw(SumMonoid::class.java).assertSatisfyingCommutativeMonoidLaw(1.toSumMonoid(), (-2).toSumMonoid(), 3.toSumMonoid())
            CommutativeMonoidLaw(LongSumMonoid::class.java).assertSatisfyingCommutativeMonoidLaw(1L.toSumMonoid(), 2L.toSumMonoid(), (-3L).toSumMonoid())
            CommutativeMonoidLaw(ProductMonoid::class.java).assertSatisfyingCommutativeMonoidLaw(2.toProductMonoid(), 3.toProductMonoid(), (-4).toProductMonoid())
            CommutativeMonoidLaw(LongProductMonoid::class.java).assertSatisfyingCommutativeMonoidLaw(2L.toProductMonoid(), 3L.toProductMonoid(), 0L.toProductMonoid())
        }
    }

    describe("boolean commutative monoids") {
        val values = listOf(true, false)
        it("should satisfy laws for every combination") {
            values.forEach { a -> values.forEach { b -> values.forEach { c ->
                CommutativeMonoidLaw(AllMonoid::class.java).assertSatisfyingCommutativeMonoidLaw(a.toAllMonoid(), b.toAllMonoid(), c.toAllMonoid())
                CommutativeMonoidLaw(AnyMonoid::class.java).assertSatisfyingCommutativeMonoidLaw(a.toAnyMonoid(), b.toAnyMonoid(), c.toAnyMonoid())
                CommutativeMonoidLaw(XorMonoid::class.java).assertSatisfyingCommutativeMonoidLaw(a.toXorMonoid(), b.toXorMonoid(), c.toXorMonoid())
            } } }
        }
    }

    describe("Set Monoid") {
        val clazz = SetMonoid::class.java as Class<SetMonoid<Int>>
        it("should satisfy laws") {
            CommutativeMonoidLaw(clazz).assertSatisfyingCommutativeMonoidLaw(setOf(1, 2).toSetMonoid(), setOf(2, 3).toSetMonoid(), setOf<Int>().toSetMonoid())
        }
        it("should satisfy band laws") { BandLaw(clazz).assertSatisfyingBandLaw(setOf(1).toSetMonoid(), setOf(1, 2).toSetMonoid(), setOf(3).toSetMonoid()) }
        it("should take the union") {
            Monoid.op(clazz, setOf(1, 2).toSetMonoid(), setOf(2, 3).toSetMonoid()) shouldBe setOf(1, 2, 3).toSetMonoid()
        }
        it("should have the empty set as identity") { Monoid.mzero(clazz) shouldBe setOf<Int>().toSetMonoid() }
    }

    describe("as a constraint") {
        fun <M: CommutativeMonoid<*, M>> combineUnordered(clazz: Class<M>, values: Set<M>): M =
            values.fold(Monoid.mzero(clazz)) { acc, m -> Monoid.op(clazz, acc, m) }

        it("should fold unordered collections") {
            combineUnordered(SumMonoid::class.java, setOf(1, 2, 3).map { it.toSumMonoid() }.toSet()) shouldBe 6.toSumMonoid()
        }
    }
})
