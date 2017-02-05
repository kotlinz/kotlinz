package com.github.kotlinz.kotlinz.type.category

import com.github.kotlinz.kotlinz.K2

interface Compose<T> {
    fun <A, B, C> compose(f: K2<T, B, C>, g: K2<T, A, B>): K2<T, A, C>
}
