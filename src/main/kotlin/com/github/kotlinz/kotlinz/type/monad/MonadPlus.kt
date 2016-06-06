package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

interface MonadPlus<T>: Monad<T> {
  fun <A> mzero(): K1<T, A>
  fun <A> mplus(m: K1<T, A>): K1<T, A>
}
