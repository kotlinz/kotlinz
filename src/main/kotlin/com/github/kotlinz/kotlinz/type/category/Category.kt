package com.github.kotlinz.kotlinz.type.category

import com.github.kotlinz.kotlinz.K3

interface Category<T, F> : Compose<T, F> {
    fun <A> id(): K3<T, F, A, A>
}
