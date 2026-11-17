package com.github.kotlinz.kotlinz.data.semiring

import com.github.kotlinz.kotlinz.type.ring.Semiring

data class BooleanSemiring(val value: Boolean) : Semiring<Boolean, BooleanSemiring> {
    constructor(): this(false)

    override fun plus(a: BooleanSemiring, b: BooleanSemiring) = (a.value || b.value).toSemiring()
    override fun times(a: BooleanSemiring, b: BooleanSemiring) = (a.value && b.value).toSemiring()
    override fun zero() = false.toSemiring()
    override fun one() = true.toSemiring()
}

fun Boolean.toSemiring() = BooleanSemiring(this)
