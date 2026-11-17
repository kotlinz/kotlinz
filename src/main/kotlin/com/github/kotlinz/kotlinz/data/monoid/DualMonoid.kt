package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.group.Monoid

data class DualMonoid<M: Monoid<*, M>>(val value: Maybe<M>) : Monoid<Maybe<M>, DualMonoid<M>> {
    constructor(): this(Maybe.None())

    override fun op(a: DualMonoid<M>, b: DualMonoid<M>) = DualMonoid(flipped(a.value, b.value))
    override fun mzero() = DualMonoid<M>(Maybe.None())

    private fun flipped(a: Maybe<M>, b: Maybe<M>) = when {
        a !is Maybe.Just -> b
        b !is Maybe.Just -> a
        else -> Maybe.Just(b.value.op(b.value, a.value))
    }
}

fun <M: Monoid<*, M>> M.toDualMonoid() = DualMonoid(Maybe.Just(this))
