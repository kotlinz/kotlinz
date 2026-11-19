package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.CommutativeMonoid
import com.github.kotlinz.kotlinz.type.group.Group

/**
 * Additive group on [Long]: identity `0L`, inverse is negation.
 *
 * ```kotlin
 * Monoid.op(LongSumMonoid::class.java, 2L.toSumMonoid(), 3L.toSumMonoid()) // LongSumMonoid(5)
 * ```
 */
data class LongSumMonoid(val value: Long) : Group<Long, LongSumMonoid>, CommutativeMonoid<Long, LongSumMonoid> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(0L)

    override fun op(a: LongSumMonoid, b: LongSumMonoid) = (a.value + b.value).toSumMonoid()
    override fun mzero() = 0L.toSumMonoid()
    override fun inverse(a: LongSumMonoid) = (-a.value).toSumMonoid()
}

/** Wraps this Long in a [LongSumMonoid]. */
fun Long.toSumMonoid() = LongSumMonoid(this)
