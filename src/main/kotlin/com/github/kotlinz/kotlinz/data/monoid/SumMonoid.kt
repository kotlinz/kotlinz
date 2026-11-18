package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Group

/**
 * Additive group on [Int]: identity `0`, inverse is negation.
 *
 * Use it with `foldMap` to sum values.
 *
 * ```kotlin
 * Monoid.op(SumMonoid::class.java, 2.toSumMonoid(), 3.toSumMonoid()) // SumMonoid(5)
 * Group.inverse(SumMonoid::class.java, 2.toSumMonoid())            // SumMonoid(-2)
 * ```
 */
data class SumMonoid(val value: Int) : Group<Int, SumMonoid> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(0)

    override fun op(a: SumMonoid, b: SumMonoid) = (a.value + b.value).toSumMonoid()
    override fun mzero() = 0.toSumMonoid()
    override fun inverse(a: SumMonoid) = (-a.value).toSumMonoid()
}

/** Wraps this Int in a [SumMonoid]. */
fun Int.toSumMonoid() = SumMonoid(this)
