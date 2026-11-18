package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * String concatenation monoid with the empty string as identity.
 *
 * ```kotlin
 * Monoid.op(StringMonoid::class.java, "foo".toStringMonoid(), "bar".toStringMonoid()) // StringMonoid("foobar")
 * ```
 */
data class StringMonoid(val value: String) : Monoid<String, StringMonoid> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this("")

    override fun op(a: StringMonoid, b: StringMonoid) = (a.value + b.value).toStringMonoid()
    override fun mzero() = "".toStringMonoid()
}

/** Wraps this String in a [StringMonoid]. */
fun String.toStringMonoid() = StringMonoid(this)
