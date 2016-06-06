package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

interface MonadOps<T>: Monad<T> {
  fun <A, B> liftM(f: (A) -> B): (K1<T, A>) -> K1<T, B>
  fun <A, B, C> liftM2(f: (A, B) -> C): (K1<T, A>, K1<T, B>) -> K1<T, C>
  fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<T, A>, K1<T, B>, K1<T, C>) -> K1<T, D>
}
