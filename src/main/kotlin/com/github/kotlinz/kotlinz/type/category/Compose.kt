package com.github.kotlinz.kotlinz.type.category

import com.github.kotlinz.kotlinz.K3

interface Compose<T, F> {
    fun <A, B, C> compose(f: K3<T, F, B, C>, g: K3<T, F, A, B>): K3<T, F, A, C>
}
