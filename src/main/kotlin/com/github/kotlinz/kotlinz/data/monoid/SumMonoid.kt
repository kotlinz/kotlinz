package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

data class SumMonoid(val value: Int) : Monoid<Int, SumMonoid> {
    constructor(): this(0)

    override fun op(a: SumMonoid, b: SumMonoid) = (a.value + b.value).toSumMonoid()
    override fun mzero() = 0.toSumMonoid()
}

fun Int.toSumMonoid() = SumMonoid(this)
