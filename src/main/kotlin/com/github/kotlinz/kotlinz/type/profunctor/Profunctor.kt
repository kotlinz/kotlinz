package com.github.kotlinz.kotlinz.type.profunctor

import com.github.kotlinz.kotlinz.K3

interface Profunctor<T, F> {
  fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<T, F, B, C>): K3<T, F, A, D>
  fun <A, B, C> lmap(f: (A) -> B, v: K3<T, F, B, C>): K3<T, F, A, C> = dimap(f, { it }, v)
  fun <A, B, C> rmap(g: (B) -> C, v: K3<T, F, A, B>): K3<T, F, A, C> = dimap({ it }, g, v)
}
