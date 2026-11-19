package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monus

/**
 * Natural numbers (non-negative [Long]s) under addition, with truncated subtraction as [Monus].
 *
 * Construction fails for negative values, and addition throws [ArithmeticException] on overflow instead of
 * wrapping, so every value stays a natural number.
 *
 * ```kotlin
 * Monus.monus(NaturalMonoid::class.java, 5L.toNaturalMonoid(), 3L.toNaturalMonoid()) // NaturalMonoid(2)
 * Monus.monus(NaturalMonoid::class.java, 3L.toNaturalMonoid(), 5L.toNaturalMonoid()) // NaturalMonoid(0)
 * ```
 */
data class NaturalMonoid(val value: Long) : Monus<Long, NaturalMonoid> {
    init {
        require(value >= 0) { "NaturalMonoid requires a non-negative value but was $value" }
    }

    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(0L)

    override fun op(a: NaturalMonoid, b: NaturalMonoid) = Math.addExact(a.value, b.value).toNaturalMonoid()
    override fun mzero() = 0L.toNaturalMonoid()
    override fun monus(a: NaturalMonoid, b: NaturalMonoid) = maxOf(0L, a.value - b.value).toNaturalMonoid()
}

/** Wraps this non-negative Long in a [NaturalMonoid]. */
fun Long.toNaturalMonoid() = NaturalMonoid(this)
