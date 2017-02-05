package com.github.kotlinz.kotlinz.type.category

interface Category<T> : Compose<T> {
    fun <A> id(a: A): A = a
}
