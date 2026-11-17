package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.lattice.BoundedLattice
import com.github.kotlinz.kotlinz.type.lattice.JoinSemilattice
import com.github.kotlinz.kotlinz.type.lattice.MeetSemilattice
import io.kotest.matchers.booleans.shouldBeTrue

class BoundedLatticeLaw<T, L: BoundedLattice<T, L>>(val clazz: Class<L>) {
    fun bottomIdentity(a: L) = JoinSemilattice.join(clazz, a, BoundedLattice.bottom(clazz)) == a
    fun topIdentity(a: L) = MeetSemilattice.meet(clazz, a, BoundedLattice.top(clazz)) == a

    fun assertSatisfyingBoundedLatticeLaw(a: L, b: L, c: L) {
        LatticeLaw(clazz).assertSatisfyingLatticeLaw(a, b, c)
        bottomIdentity(a).shouldBeTrue()
        topIdentity(a).shouldBeTrue()
    }
}
