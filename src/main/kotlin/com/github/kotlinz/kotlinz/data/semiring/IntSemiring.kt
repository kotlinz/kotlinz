package com.github.kotlinz.kotlinz.data.semiring

import com.github.kotlinz.kotlinz.type.ring.Semiring

data class IntSemiring(val value: Int) : Semiring<Int, IntSemiring> {
    constructor(): this(0)

    override fun plus(a: IntSemiring, b: IntSemiring) = (a.value + b.value).toSemiring()
    override fun times(a: IntSemiring, b: IntSemiring) = (a.value * b.value).toSemiring()
    override fun zero() = 0.toSemiring()
    override fun one() = 1.toSemiring()
}

fun Int.toSemiring() = IntSemiring(this)
