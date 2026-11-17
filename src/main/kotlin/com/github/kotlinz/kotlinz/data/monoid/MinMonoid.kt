package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.group.Monoid

data class MinMonoid<A: Comparable<A>>(val value: Maybe<A>) : Monoid<Maybe<A>, MinMonoid<A>> {
    constructor(): this(Maybe.None())

    override fun op(a: MinMonoid<A>, b: MinMonoid<A>) = if (isLeftMin(a.value, b.value)) a else b
    override fun mzero() = Maybe.None<A>().toMinMonoid()

    private fun isLeftMin(a: Maybe<A>, b: Maybe<A>) = when {
        a !is Maybe.Just -> false
        b !is Maybe.Just -> true
        else -> a.value <= b.value
    }
}

fun <A: Comparable<A>> Maybe<A>.toMinMonoid() = MinMonoid(this)
