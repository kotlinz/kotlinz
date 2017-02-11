package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

class StringMonoid(val value: String) : Monoid<String, StringMonoid> {
    constructor(): this("")

    override fun op(a: StringMonoid, b: StringMonoid) = (a.value + b.value).toMonoid()
    override fun mzero() = "".toMonoid()

    override fun toString() = value
}

fun String.toMonoid() = StringMonoid(this)
