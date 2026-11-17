package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.group.Band
import com.github.kotlinz.kotlinz.type.group.Monoid

data class FirstMonoid<A>(val value: Maybe<A>) : Monoid<Maybe<A>, FirstMonoid<A>>, Band<Maybe<A>, FirstMonoid<A>> {
    constructor(): this(Maybe.None())

    override fun op(a: FirstMonoid<A>, b: FirstMonoid<A>) = if (a.value is Maybe.Just) a else b
    override fun mzero() = Maybe.None<A>().toFirstMonoid()
}

fun <A> Maybe<A>.toFirstMonoid() = FirstMonoid(this)
