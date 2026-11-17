package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

class EndoMonoid<A>(val value: (A) -> A) : Monoid<(A) -> A, EndoMonoid<A>> {
    constructor(): this({ it })

    override fun op(a: EndoMonoid<A>, b: EndoMonoid<A>) = EndoMonoid<A> { a.value(b.value(it)) }
    override fun mzero() = EndoMonoid<A> { it }

    operator fun invoke(a: A): A = value(a)
}

fun <A> ((A) -> A).toEndoMonoid() = EndoMonoid(this)
