package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Multiplicative monoid on [Int] with identity `1`.
 *
 * ```kotlin
 * Monoid.op(ProductMonoid::class.java, 2.toProductMonoid(), 3.toProductMonoid()) // ProductMonoid(6)
 * ```
 */
data class ProductMonoid(val value: Int) : Monoid<Int, ProductMonoid> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(1)

    override fun op(a: ProductMonoid, b: ProductMonoid) = (a.value * b.value).toProductMonoid()
    override fun mzero() = 1.toProductMonoid()
}

/** Wraps this Int in a [ProductMonoid]. */
fun Int.toProductMonoid() = ProductMonoid(this)
