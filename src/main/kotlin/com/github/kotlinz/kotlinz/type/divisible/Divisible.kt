package com.github.kotlinz.kotlinz.type.divisible

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Contravariant

interface Divisible<T>: Contravariant<T> {
  fun <A, B, C> divide(f: (A) -> Pair<B, C>, fb: K1<T, B>, fc: K1<T, C>): K1<T, A>
  fun <A> conquer(): K1<T, A>
}
