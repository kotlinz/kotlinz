package com.github.kotlinz.kotlinz.type.arrow

import com.github.kotlinz.kotlinz.K3

interface ArrowApply<T, F> : Arrow<T, F> {
    fun <A, B> app(): K3<T, F, Pair<K3<T, F, A, B>, A>, B>
}
