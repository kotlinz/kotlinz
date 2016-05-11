package com.github.unhappychoice.kotlinz.type.pointed

import com.github.unhappychoice.kotlinz.K1

interface Pointed<µ> {
  fun <A> pure(v: A): K1<µ, A>
}
