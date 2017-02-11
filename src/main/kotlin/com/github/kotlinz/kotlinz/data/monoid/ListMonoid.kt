package com.github.kotlinz.kotlinz.data.monoid

import com.github.kotlinz.kotlinz.type.group.Monoid

data class ListMonoid(val value: List<*>) : Monoid<List<*>, ListMonoid> {
    constructor(): this(listOf<Any>())

    override fun op(a: ListMonoid, b: ListMonoid) = (a.value + b.value).toListMonoid()
    override fun mzero() = listOf<Any>().toListMonoid()
}

fun <E> List<E>.toListMonoid() = ListMonoid(this)