package com.github.kotlinz.kotlinz.data.semiring

import com.github.kotlinz.kotlinz.type.ring.Ring

/**
 * Long ring: ordinary addition (zero = `0L`), multiplication (one = `1L`) and negation; overflow wraps.
 *
 * Wraps a [Long] so it can be used wherever a [com.github.kotlinz.kotlinz.type.ring.Semiring] or [Ring]
 * is required. Because overflow wraps, this is the ring of integers modulo 2^64 and the laws hold exactly.
 *
 * ```kotlin
 * Semiring.plus(LongSemiring::class.java, 2L.toSemiring(), 3L.toSemiring()) // 5L.toSemiring()
 * Ring.negate(LongSemiring::class.java, 2L.toSemiring())                    // (-2L).toSemiring()
 * ```
 */
data class LongSemiring(val value: Long) : Ring<Long, LongSemiring> {
    /** Creates the zero element; used for reflective instance lookup. */
    constructor(): this(0L)

    override fun plus(a: LongSemiring, b: LongSemiring) = (a.value + b.value).toSemiring()
    override fun times(a: LongSemiring, b: LongSemiring) = (a.value * b.value).toSemiring()
    override fun zero() = 0L.toSemiring()
    override fun one() = 1L.toSemiring()
    override fun negate(a: LongSemiring) = (-a.value).toSemiring()
}

/** Wraps this value in a [LongSemiring]. */
fun Long.toSemiring() = LongSemiring(this)
