package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1

interface Contravariant<T> {
  fun <A, B> contramap(f: (B) -> A, v: K1<T, A>): K1<T, B>
}
