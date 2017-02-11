package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

data class StringMonoid(val value: String) : Monoid<String, StringMonoid> {
    constructor(): this("")

    override fun op(a: StringMonoid, b: StringMonoid) = (a.value + b.value).toStringMonoid()
    override fun mzero() = "".toStringMonoid()
}

fun String.toStringMonoid() = StringMonoid(this)
