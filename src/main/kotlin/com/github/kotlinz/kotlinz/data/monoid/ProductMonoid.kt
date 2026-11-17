package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

data class ProductMonoid(val value: Int) : Monoid<Int, ProductMonoid> {
    constructor(): this(1)

    override fun op(a: ProductMonoid, b: ProductMonoid) = (a.value * b.value).toProductMonoid()
    override fun mzero() = 1.toProductMonoid()
}

fun Int.toProductMonoid() = ProductMonoid(this)
