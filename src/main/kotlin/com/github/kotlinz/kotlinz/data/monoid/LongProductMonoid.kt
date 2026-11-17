package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

data class LongProductMonoid(val value: Long) : Monoid<Long, LongProductMonoid> {
    constructor(): this(1L)

    override fun op(a: LongProductMonoid, b: LongProductMonoid) = (a.value * b.value).toProductMonoid()
    override fun mzero() = 1L.toProductMonoid()
}

fun Long.toProductMonoid() = LongProductMonoid(this)
