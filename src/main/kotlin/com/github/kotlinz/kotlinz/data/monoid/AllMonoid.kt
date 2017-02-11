package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

data class AllMonoid(val value: Boolean) : Monoid<Boolean, AllMonoid> {
    constructor(): this(true)

    override fun op(a: AllMonoid, b: AllMonoid) = (a.value && b.value).toAllMonoid()
    override fun mzero() = true.toAllMonoid()
}

fun Boolean.toAllMonoid() = AllMonoid(this)