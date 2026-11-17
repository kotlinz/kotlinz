package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Alternative

interface MonadPlus<T>: Monad<T>, Alternative<T> {
  fun <A> mzero(): K1<T, A> = empty()
  fun <A> mplus(m1: K1<T, A>, m2: K1<T, A>): K1<T, A> = orElse(m1, m2)
}
