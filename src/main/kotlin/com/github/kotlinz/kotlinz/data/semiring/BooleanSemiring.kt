package com.github.kotlinz.kotlinz.data.semiring

import com.github.kotlinz.kotlinz.type.ring.Semiring

/**
 * Boolean semiring: `plus` is logical or (zero = `false`), `times` is logical and (one = `true`).
 *
 * Wraps a [Boolean] so it can be used wherever a [Semiring] is required.
 *
 * ```kotlin
 * Semiring.plus(BooleanSemiring::class.java, true.toSemiring(), false.toSemiring()) // true.toSemiring()
 * ```
 */
data class BooleanSemiring(val value: Boolean) : Semiring<Boolean, BooleanSemiring> {
    /** Creates the zero element; used for reflective instance lookup. */
    constructor(): this(false)

    override fun plus(a: BooleanSemiring, b: BooleanSemiring) = (a.value || b.value).toSemiring()
    override fun times(a: BooleanSemiring, b: BooleanSemiring) = (a.value && b.value).toSemiring()
    override fun zero() = false.toSemiring()
    override fun one() = true.toSemiring()
}

/** Wraps this value in a [BooleanSemiring]. */
fun Boolean.toSemiring() = BooleanSemiring(this)
