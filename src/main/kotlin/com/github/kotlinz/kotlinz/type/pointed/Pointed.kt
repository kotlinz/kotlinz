package com.github.kotlinz.kotlinz.type.pointed

import com.github.kotlinz.kotlinz.K1

interface Pointed<T> {
  fun <A> pure(v: A): K1<T, A>
}
