package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * The dual of a monoid [M]: combines in the opposite order (`op(a, b) = M.op(b, a)`).
 *
 * The identity is [Maybe.None], so no instance of [M]'s own identity is needed. Use it to reverse
 * the combination order of a non-commutative monoid such as [StringMonoid].
 *
 * ```kotlin
 * val clazz = DualMonoid::class.java as Class<DualMonoid<StringMonoid>>
 * Monoid.op(clazz, "a".toStringMonoid().toDualMonoid(), "b".toStringMonoid().toDualMonoid())
 * // DualMonoid(Just(StringMonoid("ba")))
 * ```
 */
data class DualMonoid<M: Monoid<*, M>>(val value: Maybe<M>) : Monoid<Maybe<M>, DualMonoid<M>> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(Maybe.None())

    override fun op(a: DualMonoid<M>, b: DualMonoid<M>) = DualMonoid(flipped(a.value, b.value))
    override fun mzero() = DualMonoid<M>(Maybe.None())

    private fun flipped(a: Maybe<M>, b: Maybe<M>) = when {
        a !is Maybe.Just -> b
        b !is Maybe.Just -> a
        else -> Maybe.Just(b.value.op(b.value, a.value))
    }
}

/** Wraps this monoid value in a [DualMonoid]. */
fun <M: Monoid<*, M>> M.toDualMonoid() = DualMonoid(Maybe.Just(this))
