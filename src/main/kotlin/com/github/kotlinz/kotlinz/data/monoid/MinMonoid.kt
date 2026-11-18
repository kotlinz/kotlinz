package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.group.Band
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Monoid that keeps the minimum value, with [Maybe.None] as identity (no bounded type needed). Also a Band.
 *
 * On ties the left operand is kept.
 *
 * ```kotlin
 * val clazz = MinMonoid::class.java as Class<MinMonoid<Int>>
 * Monoid.op(clazz, Maybe.Just(1).toMinMonoid(), Maybe.Just(3).toMinMonoid()) // MinMonoid(Just(1))
 * ```
 */
data class MinMonoid<A: Comparable<A>>(val value: Maybe<A>) : Monoid<Maybe<A>, MinMonoid<A>>, Band<Maybe<A>, MinMonoid<A>> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(Maybe.None())

    override fun op(a: MinMonoid<A>, b: MinMonoid<A>) = if (isLeftMin(a.value, b.value)) a else b
    override fun mzero() = Maybe.None<A>().toMinMonoid()

    private fun isLeftMin(a: Maybe<A>, b: Maybe<A>) = when {
        a !is Maybe.Just -> false
        b !is Maybe.Just -> true
        else -> a.value <= b.value
    }
}

/** Wraps this [Maybe] in a [MinMonoid]. */
fun <A: Comparable<A>> Maybe<A>.toMinMonoid() = MinMonoid(this)
