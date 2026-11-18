package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.group.Band
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Monoid that keeps the leftmost [Maybe.Just], with [Maybe.None] as identity. Also a Band.
 *
 * ```kotlin
 * val clazz = FirstMonoid::class.java as Class<FirstMonoid<Int>>
 * Monoid.op(clazz, Maybe.Just(1).toFirstMonoid(), Maybe.Just(2).toFirstMonoid()) // FirstMonoid(Just(1))
 * ```
 */
data class FirstMonoid<A>(val value: Maybe<A>) : Monoid<Maybe<A>, FirstMonoid<A>>, Band<Maybe<A>, FirstMonoid<A>> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(Maybe.None())

    override fun op(a: FirstMonoid<A>, b: FirstMonoid<A>) = if (a.value is Maybe.Just) a else b
    override fun mzero() = Maybe.None<A>().toFirstMonoid()
}

/** Wraps this [Maybe] in a [FirstMonoid]. */
fun <A> Maybe<A>.toFirstMonoid() = FirstMonoid(this)
