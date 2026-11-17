package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Group

data class SumMonoid(val value: Int) : Group<Int, SumMonoid> {
    constructor(): this(0)

    override fun op(a: SumMonoid, b: SumMonoid) = (a.value + b.value).toSumMonoid()
    override fun mzero() = 0.toSumMonoid()
    override fun inverse(a: SumMonoid) = (-a.value).toSumMonoid()
}

fun Int.toSumMonoid() = SumMonoid(this)
