package com.github.kotlinz.kotlinz.data.semiring

import com.github.kotlinz.kotlinz.type.ring.Semiring

data class LongSemiring(val value: Long) : Semiring<Long, LongSemiring> {
    constructor(): this(0L)

    override fun plus(a: LongSemiring, b: LongSemiring) = (a.value + b.value).toSemiring()
    override fun times(a: LongSemiring, b: LongSemiring) = (a.value * b.value).toSemiring()
    override fun zero() = 0L.toSemiring()
    override fun one() = 1L.toSemiring()
}

fun Long.toSemiring() = LongSemiring(this)
