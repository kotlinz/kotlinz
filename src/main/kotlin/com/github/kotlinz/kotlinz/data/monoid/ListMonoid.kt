package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * List concatenation monoid with the empty list as identity.
 *
 * The element type is erased (`List<*>`), so callers must cast when reading [value].
 *
 * ```kotlin
 * Monoid.op(ListMonoid::class.java, listOf(1).toListMonoid(), listOf(2, 3).toListMonoid()) // ListMonoid([1, 2, 3])
 * ```
 */
data class ListMonoid(val value: List<*>) : Monoid<List<*>, ListMonoid> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(listOf<Any>())

    override fun op(a: ListMonoid, b: ListMonoid) = (a.value + b.value).toListMonoid()
    override fun mzero() = listOf<Any>().toListMonoid()
}

/** Wraps this list in a [ListMonoid]. */
fun <E> List<E>.toListMonoid() = ListMonoid(this)