package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Group

/**
 * Boolean exclusive-or group: identity `false`, every element is its own inverse.
 *
 * ```kotlin
 * Monoid.op(XorMonoid::class.java, true.toXorMonoid(), true.toXorMonoid()) // XorMonoid(false)
 * ```
 */
data class XorMonoid(val value: Boolean) : Group<Boolean, XorMonoid> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(false)

    override fun op(a: XorMonoid, b: XorMonoid) = (a.value xor b.value).toXorMonoid()
    override fun mzero() = false.toXorMonoid()
    override fun inverse(a: XorMonoid) = a
}

/** Wraps this Boolean in a [XorMonoid]. */
fun Boolean.toXorMonoid() = XorMonoid(this)
