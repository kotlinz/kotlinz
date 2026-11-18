package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Monoid of endofunctions `(A) -> A` under composition, with the identity function as identity.
 *
 * `op(f, g)` composes right to left, i.e. applies `g` first. It is not a data class, so equality
 * is reference equality; compare by applying to inputs.
 *
 * ```kotlin
 * val clazz = EndoMonoid::class.java as Class<EndoMonoid<Int>>
 * val inc = { a: Int -> a + 1 }.toEndoMonoid()
 * val double = { a: Int -> a * 2 }.toEndoMonoid()
 * Monoid.op(clazz, inc, double)(3) // 7
 * ```
 */
class EndoMonoid<A>(val value: (A) -> A) : Monoid<(A) -> A, EndoMonoid<A>> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this({ it })

    override fun op(a: EndoMonoid<A>, b: EndoMonoid<A>) = EndoMonoid<A> { a.value(b.value(it)) }
    override fun mzero() = EndoMonoid<A> { it }

    /** Applies the wrapped function to [a]. */
    operator fun invoke(a: A): A = value(a)
}

/** Wraps this function in an [EndoMonoid]. */
fun <A> ((A) -> A).toEndoMonoid() = EndoMonoid(this)
