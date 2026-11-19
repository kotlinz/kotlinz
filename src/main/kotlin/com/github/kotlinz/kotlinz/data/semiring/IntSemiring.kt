package com.github.kotlinz.kotlinz.data.semiring

import com.github.kotlinz.kotlinz.type.ring.Ring

/**
 * Int ring: ordinary addition (zero = `0`), multiplication (one = `1`) and negation; overflow wraps.
 *
 * Wraps a [Int] so it can be used wherever a [com.github.kotlinz.kotlinz.type.ring.Semiring] or [Ring]
 * is required. Because overflow wraps, this is the ring of integers modulo 2^32 and the laws hold exactly.
 *
 * ```kotlin
 * Semiring.plus(IntSemiring::class.java, 2.toSemiring(), 3.toSemiring()) // 5.toSemiring()
 * Ring.minus(IntSemiring::class.java, 2.toSemiring(), 3.toSemiring())    // (-1).toSemiring()
 * ```
 */
data class IntSemiring(val value: Int) : Ring<Int, IntSemiring> {
    /** Creates the zero element; used for reflective instance lookup. */
    constructor(): this(0)

    override fun plus(a: IntSemiring, b: IntSemiring) = (a.value + b.value).toSemiring()
    override fun times(a: IntSemiring, b: IntSemiring) = (a.value * b.value).toSemiring()
    override fun zero() = 0.toSemiring()
    override fun one() = 1.toSemiring()
    override fun negate(a: IntSemiring) = (-a.value).toSemiring()
}

/** Wraps this value in a [IntSemiring]. */
fun Int.toSemiring() = IntSemiring(this)
