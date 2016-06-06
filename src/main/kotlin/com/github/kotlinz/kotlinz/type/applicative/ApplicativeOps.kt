package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1

interface ApplicativeOps<T>: Applicative<T> {
  fun <A, B> liftA(f: (A) -> B): (K1<T, A>) -> K1<T, B>
  fun <A, B, C> liftA2(f: (A, B) -> C): (K1<T, A>, K1<T, B>) -> K1<T, C>
  fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<T, A>, K1<T, B>, K1<T, C>) -> K1<T, D>
}