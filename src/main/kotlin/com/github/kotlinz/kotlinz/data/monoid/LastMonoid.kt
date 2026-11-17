package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.group.Band
import com.github.kotlinz.kotlinz.type.group.Monoid

data class LastMonoid<A>(val value: Maybe<A>) : Monoid<Maybe<A>, LastMonoid<A>>, Band<Maybe<A>, LastMonoid<A>> {
    constructor(): this(Maybe.None())

    override fun op(a: LastMonoid<A>, b: LastMonoid<A>) = if (b.value is Maybe.Just) b else a
    override fun mzero() = Maybe.None<A>().toLastMonoid()
}

fun <A> Maybe<A>.toLastMonoid() = LastMonoid(this)
