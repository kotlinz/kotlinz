package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Band
import com.github.kotlinz.kotlinz.type.group.CommutativeMonoid
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Boolean disjunction monoid: combines with `||`, identity `false`. Also a Band (idempotent).
 *
 * Use it to check whether any element satisfies a condition via `foldMap`.
 *
 * ```kotlin
 * Monoid.op(AnyMonoid::class.java, true.toAnyMonoid(), false.toAnyMonoid()) // AnyMonoid(true)
 * ```
 */
data class AnyMonoid(val value: Boolean) : Monoid<Boolean, AnyMonoid>, CommutativeMonoid<Boolean, AnyMonoid>, Band<Boolean, AnyMonoid> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(false)

    override fun op(a: AnyMonoid, b: AnyMonoid) = (a.value || b.value).toAnyMonoid()
    override fun mzero() = false.toAnyMonoid()
}

/** Wraps this Boolean in an [AnyMonoid]. */
fun Boolean.toAnyMonoid() = AnyMonoid(this)