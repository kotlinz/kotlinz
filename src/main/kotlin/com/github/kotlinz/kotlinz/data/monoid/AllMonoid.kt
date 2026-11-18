package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Band
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Boolean conjunction monoid: combines with `&&`, identity `true`. Also a Band (idempotent).
 *
 * Use it to check that every element satisfies a condition via `foldMap`.
 *
 * ```kotlin
 * Monoid.op(AllMonoid::class.java, true.toAllMonoid(), false.toAllMonoid()) // AllMonoid(false)
 * ```
 */
data class AllMonoid(val value: Boolean) : Monoid<Boolean, AllMonoid>, Band<Boolean, AllMonoid> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(true)

    override fun op(a: AllMonoid, b: AllMonoid) = (a.value && b.value).toAllMonoid()
    override fun mzero() = true.toAllMonoid()
}

/** Wraps this Boolean in an [AllMonoid]. */
fun Boolean.toAllMonoid() = AllMonoid(this)