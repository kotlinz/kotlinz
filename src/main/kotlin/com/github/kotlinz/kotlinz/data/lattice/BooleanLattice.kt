package com.github.kotlinz.kotlinz.data.lattice

import com.github.kotlinz.kotlinz.type.lattice.BoundedLattice

data class BooleanLattice(val value: Boolean) : BoundedLattice<Boolean, BooleanLattice> {
    constructor(): this(false)

    override fun join(a: BooleanLattice, b: BooleanLattice) = (a.value || b.value).toLattice()
    override fun meet(a: BooleanLattice, b: BooleanLattice) = (a.value && b.value).toLattice()
    override fun top() = true.toLattice()
    override fun bottom() = false.toLattice()
}

fun Boolean.toLattice() = BooleanLattice(this)
