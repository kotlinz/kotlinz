package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

data class AnyMonoid(val value: Boolean) : Monoid<Boolean, AnyMonoid> {
    constructor(): this(false)

    override fun op(a: AnyMonoid, b: AnyMonoid) = (a.value || b.value).toAnyMonoid()
    override fun mzero() = false.toAnyMonoid()
}

fun Boolean.toAnyMonoid() = AnyMonoid(this)