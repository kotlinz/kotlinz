package com.github.unhappychoice.kotlinz.type.pointed

import com.github.unhappychoice.kotlinz.K1

interface Copointed<T> {
  fun <A> extract(v: K1<T, A>): A
}
