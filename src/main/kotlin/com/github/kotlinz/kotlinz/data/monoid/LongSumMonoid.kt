package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

data class LongSumMonoid(val value: Long) : Monoid<Long, LongSumMonoid> {
    constructor(): this(0L)

    override fun op(a: LongSumMonoid, b: LongSumMonoid) = (a.value + b.value).toSumMonoid()
    override fun mzero() = 0L.toSumMonoid()
}

fun Long.toSumMonoid() = LongSumMonoid(this)
