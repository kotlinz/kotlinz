package com.github.unhappychoice.kotlinz.type

import com.github.unhappychoice.kotlinz.K1

interface Functor<T> {
  fun <A, B> fmap(f: (A) -> B, v: K1<T, A>): K1<T, B>
}