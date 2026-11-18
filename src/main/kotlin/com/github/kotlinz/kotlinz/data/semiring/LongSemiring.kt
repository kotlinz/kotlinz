package com.github.kotlinz.kotlinz.data.semiring

import com.github.kotlinz.kotlinz.type.ring.Semiring

/**
 * Long semiring: ordinary addition (zero = `0L`) and multiplication (one = `1L`); overflow wraps.
 *
 * Wraps a [Long] so it can be used wherever a [Semiring] is required.
 *
 * ```kotlin
 * Semiring.plus(LongSemiring::class.java, 2L.toSemiring(), 3L.toSemiring()) // 5L.toSemiring()
 * ```
 */
data class LongSemiring(val value: Long) : Semiring<Long, LongSemiring> {
    /** Creates the zero element; used for reflective instance lookup. */
    constructor(): this(0L)

    override fun plus(a: LongSemiring, b: LongSemiring) = (a.value + b.value).toSemiring()
    override fun times(a: LongSemiring, b: LongSemiring) = (a.value * b.value).toSemiring()
    override fun zero() = 0L.toSemiring()
    override fun one() = 1L.toSemiring()
}

/** Wraps this value in a [LongSemiring]. */
fun Long.toSemiring() = LongSemiring(this)
