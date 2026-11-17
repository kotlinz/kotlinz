package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Group

data class XorMonoid(val value: Boolean) : Group<Boolean, XorMonoid> {
    constructor(): this(false)

    override fun op(a: XorMonoid, b: XorMonoid) = (a.value xor b.value).toXorMonoid()
    override fun mzero() = false.toXorMonoid()
    override fun inverse(a: XorMonoid) = a
}

fun Boolean.toXorMonoid() = XorMonoid(this)
