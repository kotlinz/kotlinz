package com.github.unhappychoice.kotlinz.type.pointed

import com.github.unhappychoice.kotlinz.K1

interface Copointed<µ> {
  fun <A> extract(v: K1<µ, A>): A
}
