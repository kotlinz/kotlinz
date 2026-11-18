package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.group.Band
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Monoid that keeps the rightmost [Maybe.Just], with [Maybe.None] as identity. Also a Band.
 *
 * ```kotlin
 * val clazz = LastMonoid::class.java as Class<LastMonoid<Int>>
 * Monoid.op(clazz, Maybe.Just(1).toLastMonoid(), Maybe.Just(2).toLastMonoid()) // LastMonoid(Just(2))
 * ```
 */
data class LastMonoid<A>(val value: Maybe<A>) : Monoid<Maybe<A>, LastMonoid<A>>, Band<Maybe<A>, LastMonoid<A>> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(Maybe.None())

    override fun op(a: LastMonoid<A>, b: LastMonoid<A>) = if (b.value is Maybe.Just) b else a
    override fun mzero() = Maybe.None<A>().toLastMonoid()
}

/** Wraps this [Maybe] in a [LastMonoid]. */
fun <A> Maybe<A>.toLastMonoid() = LastMonoid(this)
