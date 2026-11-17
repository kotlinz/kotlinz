package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K2

interface Bifunctor<T> {
  fun <A, B, C, D> bimap(f: (A) -> C, g: (B) -> D, v: K2<T, A, B>): K2<T, C, D>
  fun <A, B, C> first(f: (A) -> C, v: K2<T, A, B>): K2<T, C, B> = bimap(f, { it }, v)
  fun <A, B, D> second(g: (B) -> D, v: K2<T, A, B>): K2<T, A, D> = bimap({ it }, g, v)
}
