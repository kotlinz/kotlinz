package com.github.unhappychoice.kotlinz.type.monad

import com.github.unhappychoice.kotlinz.K1

interface MonadPlus<T>: Monad<T> {
  fun <A> mzero(): K1<T, A>
  fun <A> mplus(m: K1<T, A>): K1<T, A>
}
