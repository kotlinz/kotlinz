package com.github.kotlinz.kotlinz.data.lattice

import com.github.kotlinz.kotlinz.type.lattice.Lattice

/**
 * Lattice of sets: join is union and meet is intersection. Not bounded, as there is no universal top set.
 *
 * ```kotlin
 * val clazz = SetLattice::class.java as Class<SetLattice<Int>>
 * JoinSemilattice.join(clazz, setOf(1, 2).toLattice(), setOf(2, 3).toLattice()) // SetLattice({1, 2, 3})
 * MeetSemilattice.meet(clazz, setOf(1, 2).toLattice(), setOf(2, 3).toLattice()) // SetLattice({2})
 * ```
 */
data class SetLattice<A>(val value: Set<A>) : Lattice<Set<A>, SetLattice<A>> {
    /** Creates the empty set; required by reflective lookups such as `JoinSemilattice.join(type, a, b)`. */
    constructor(): this(setOf())

    override fun join(a: SetLattice<A>, b: SetLattice<A>) = (a.value union b.value).toLattice()
    override fun meet(a: SetLattice<A>, b: SetLattice<A>) = (a.value intersect b.value).toLattice()
}

/** Wraps this set in a [SetLattice]. */
fun <A> Set<A>.toLattice() = SetLattice(this)
