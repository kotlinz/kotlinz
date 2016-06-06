package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

interface MonadZip<T>: Monad<T> {
  fun <A, B> mzip(m1: K1<T, A>, m2: K1<T, B>): K1<T, Pair<A, B>>
  fun <A, B, C> mzipWith(m1: K1<T, A>, m2: K1<T, B>, f: (A, B) -> C): K1<T, C>
  fun <A, B> munzip(m : K1<T, Pair<A, B>>): Pair<K1<T, A>, K1<T, B>>
}
