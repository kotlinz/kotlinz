package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.type.lattice.JoinSemilattice
import com.github.kotlinz.kotlinz.type.lattice.Lattice
import com.github.kotlinz.kotlinz.type.lattice.MeetSemilattice
import io.kotest.matchers.booleans.shouldBeTrue

class LatticeLaw<T, L: Lattice<T, L>>(val clazz: Class<L>) {
    fun joinAssociativity(a: L, b: L, c: L) = join(join(a, b), c) == join(a, join(b, c))
    fun joinCommutativity(a: L, b: L) = join(a, b) == join(b, a)
    fun joinIdempotency(a: L) = join(a, a) == a

    fun meetAssociativity(a: L, b: L, c: L) = meet(meet(a, b), c) == meet(a, meet(b, c))
    fun meetCommutativity(a: L, b: L) = meet(a, b) == meet(b, a)
    fun meetIdempotency(a: L) = meet(a, a) == a

    fun absorption(a: L, b: L) = join(a, meet(a, b)) == a && meet(a, join(a, b)) == a

    fun assertSatisfyingLatticeLaw(a: L, b: L, c: L) {
        listOf(
                joinAssociativity(a, b, c), joinCommutativity(a, b), joinIdempotency(a),
                meetAssociativity(a, b, c), meetCommutativity(a, b), meetIdempotency(a),
                absorption(a, b)
        ).forEach { it.shouldBeTrue() }
    }

    private fun join(a: L, b: L) = JoinSemilattice.join(clazz, a, b)
    private fun meet(a: L, b: L) = MeetSemilattice.meet(clazz, a, b)
}
