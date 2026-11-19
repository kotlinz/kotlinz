package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Band
import com.github.kotlinz.kotlinz.type.group.Monus

/**
 * Set union monoid: combines with `union`, identity is the empty set. Commutative and a Band (idempotent),
 * and a [Monus] whose truncated subtraction is set difference.
 *
 * ```kotlin
 * val clazz = SetMonoid::class.java as Class<SetMonoid<Int>>
 * Monoid.op(clazz, setOf(1, 2).toSetMonoid(), setOf(2, 3).toSetMonoid())    // SetMonoid({1, 2, 3})
 * Monus.monus(clazz, setOf(1, 2).toSetMonoid(), setOf(2, 3).toSetMonoid()) // SetMonoid({1})
 * ```
 */
data class SetMonoid<A>(val value: Set<A>) : Monus<Set<A>, SetMonoid<A>>, Band<Set<A>, SetMonoid<A>> {
    /** Creates the identity element; required by reflective lookups such as `Monoid.mzero(type)`. */
    constructor(): this(setOf())

    override fun op(a: SetMonoid<A>, b: SetMonoid<A>) = (a.value union b.value).toSetMonoid()
    override fun mzero() = setOf<A>().toSetMonoid()
    override fun monus(a: SetMonoid<A>, b: SetMonoid<A>) = (a.value subtract b.value).toSetMonoid()
}

/** Wraps this set in a [SetMonoid]. */
fun <A> Set<A>.toSetMonoid() = SetMonoid(this)
