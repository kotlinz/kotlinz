package com.github.kotlinz.kotlinz.data.semiring

import com.github.kotlinz.kotlinz.type.ring.Semiring

/**
 * Int semiring: ordinary addition (zero = `0`) and multiplication (one = `1`); overflow wraps.
 *
 * Wraps a [Int] so it can be used wherever a [Semiring] is required.
 *
 * ```kotlin
 * Semiring.plus(IntSemiring::class.java, 2.toSemiring(), 3.toSemiring()) // 5.toSemiring()
 * ```
 */
data class IntSemiring(val value: Int) : Semiring<Int, IntSemiring> {
    /** Creates the zero element; used for reflective instance lookup. */
    constructor(): this(0)

    override fun plus(a: IntSemiring, b: IntSemiring) = (a.value + b.value).toSemiring()
    override fun times(a: IntSemiring, b: IntSemiring) = (a.value * b.value).toSemiring()
    override fun zero() = 0.toSemiring()
    override fun one() = 1.toSemiring()
}

/** Wraps this value in a [IntSemiring]. */
fun Int.toSemiring() = IntSemiring(this)
