package com.github.kotlinz.kotlinz.data.lattice

import com.github.kotlinz.kotlinz.law.BoundedLatticeLaw
import com.github.kotlinz.kotlinz.law.LatticeLaw
import com.github.kotlinz.kotlinz.type.lattice.BoundedLattice
import com.github.kotlinz.kotlinz.type.lattice.JoinSemilattice
import com.github.kotlinz.kotlinz.type.lattice.MeetSemilattice
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@Suppress("UNCHECKED_CAST")
class LatticeTest : DescribeSpec({
    describe("Boolean Lattice") {
        val clazz = BooleanLattice::class.java
        val law = BoundedLatticeLaw(clazz)
        it("should satisfy laws") {
            law.assertSatisfyingBoundedLatticeLaw(true.toLattice(), false.toLattice(), true.toLattice())
            law.assertSatisfyingBoundedLatticeLaw(false.toLattice(), true.toLattice(), false.toLattice())
        }
        it("should have bounds") {
            BoundedLattice.top(clazz) shouldBe true.toLattice()
            BoundedLattice.bottom(clazz) shouldBe false.toLattice()
        }
    }

    describe("Set Lattice") {
        val clazz = SetLattice::class.java as Class<SetLattice<Int>>
        val law = LatticeLaw(clazz)
        val a = setOf(1, 2).toLattice()
        val b = setOf(2, 3).toLattice()
        it("should satisfy laws") { law.assertSatisfyingLatticeLaw(a, b, setOf(3, 4).toLattice()) }
        it("should join with union") { JoinSemilattice.join(clazz, a, b) shouldBe setOf(1, 2, 3).toLattice() }
        it("should meet with intersection") { MeetSemilattice.meet(clazz, a, b) shouldBe setOf(2).toLattice() }
    }
})
