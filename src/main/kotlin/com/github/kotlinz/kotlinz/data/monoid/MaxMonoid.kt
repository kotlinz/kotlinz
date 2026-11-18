package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.group.Band
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Monoid that keeps the maximum value, with [Maybe.None] as identity (no bounded type needed). Also a Band.
 *
 * On ties the left operand is kept.
 *
 * ```kotlin
 * val clazz = MaxMonoid::class.java as Class<MaxMonoid<Int>>
 * Monoid.op(clazz, Maybe.Just(1).toMaxMonoid(), Maybe.Just(3).toMaxMonoid()) // MaxMonoid(Just(3))
 * ```
 */
data class MaxMonoid<A: Comparable<A>>(val value: Maybe<A>) : Monoid<Maybe<A>, MaxMonoid<A>>, Band<Maybe<A>, MaxMonoid<A>> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(Maybe.None())

    override fun op(a: MaxMonoid<A>, b: MaxMonoid<A>) = if (isLeftMax(a.value, b.value)) a else b
    override fun mzero() = Maybe.None<A>().toMaxMonoid()

    private fun isLeftMax(a: Maybe<A>, b: Maybe<A>) = when {
        a !is Maybe.Just -> false
        b !is Maybe.Just -> true
        else -> a.value >= b.value
    }
}

/** Wraps this [Maybe] in a [MaxMonoid]. */
fun <A: Comparable<A>> Maybe<A>.toMaxMonoid() = MaxMonoid(this)
