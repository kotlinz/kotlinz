package com.github.unhappychoice.kotlinz.type.pointed

import com.github.unhappychoice.kotlinz.K1

interface Pointed<T> {
  fun <A> pure(v: A): K1<T, A>
}
