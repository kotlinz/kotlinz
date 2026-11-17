package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.group.Monoid

data class MaxMonoid<A: Comparable<A>>(val value: Maybe<A>) : Monoid<Maybe<A>, MaxMonoid<A>> {
    constructor(): this(Maybe.None())

    override fun op(a: MaxMonoid<A>, b: MaxMonoid<A>) = if (isLeftMax(a.value, b.value)) a else b
    override fun mzero() = Maybe.None<A>().toMaxMonoid()

    private fun isLeftMax(a: Maybe<A>, b: Maybe<A>) = when {
        a !is Maybe.Just -> false
        b !is Maybe.Just -> true
        else -> a.value >= b.value
    }
}

fun <A: Comparable<A>> Maybe<A>.toMaxMonoid() = MaxMonoid(this)
