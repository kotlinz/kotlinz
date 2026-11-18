package com.github.kotlinz.kotlinz.data.lattice

import com.github.kotlinz.kotlinz.type.lattice.BoundedLattice

/**
 * Bounded lattice on [Boolean]: join is `||`, meet is `&&`, top is `true` and bottom is `false`.
 *
 * ```kotlin
 * JoinSemilattice.join(BooleanLattice::class.java, true.toLattice(), false.toLattice()) // BooleanLattice(true)
 * BoundedLattice.top(BooleanLattice::class.java)                                    // BooleanLattice(true)
 * ```
 */
data class BooleanLattice(val value: Boolean) : BoundedLattice<Boolean, BooleanLattice> {
    /** Creates the bottom element; required by reflective lookups such as `BoundedLattice.top(type)`. */
    constructor(): this(false)

    override fun join(a: BooleanLattice, b: BooleanLattice) = (a.value || b.value).toLattice()
    override fun meet(a: BooleanLattice, b: BooleanLattice) = (a.value && b.value).toLattice()
    override fun top() = true.toLattice()
    override fun bottom() = false.toLattice()
}

/** Wraps this Boolean in a [BooleanLattice]. */
fun Boolean.toLattice() = BooleanLattice(this)
