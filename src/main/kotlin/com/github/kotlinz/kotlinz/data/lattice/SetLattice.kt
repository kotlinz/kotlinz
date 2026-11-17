package com.github.kotlinz.kotlinz.data.lattice

import com.github.kotlinz.kotlinz.type.lattice.Lattice

data class SetLattice<A>(val value: Set<A>) : Lattice<Set<A>, SetLattice<A>> {
    constructor(): this(setOf())

    override fun join(a: SetLattice<A>, b: SetLattice<A>) = (a.value union b.value).toLattice()
    override fun meet(a: SetLattice<A>, b: SetLattice<A>) = (a.value intersect b.value).toLattice()
}

fun <A> Set<A>.toLattice() = SetLattice(this)
