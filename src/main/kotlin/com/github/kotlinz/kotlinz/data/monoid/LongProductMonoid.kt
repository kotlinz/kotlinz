package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Multiplicative monoid on [Long] with identity `1L`.
 *
 * ```kotlin
 * Monoid.op(LongProductMonoid::class.java, 2L.toProductMonoid(), 3L.toProductMonoid()) // LongProductMonoid(6)
 * ```
 */
data class LongProductMonoid(val value: Long) : Monoid<Long, LongProductMonoid> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(1L)

    override fun op(a: LongProductMonoid, b: LongProductMonoid) = (a.value * b.value).toProductMonoid()
    override fun mzero() = 1L.toProductMonoid()
}

/** Wraps this Long in a [LongProductMonoid]. */
fun Long.toProductMonoid() = LongProductMonoid(this)
