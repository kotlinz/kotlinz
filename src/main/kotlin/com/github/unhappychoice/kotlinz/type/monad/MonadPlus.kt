package com.github.unhappychoice.kotlinz.type.monad

import com.github.unhappychoice.kotlinz.K1

interface MonadPlus<µ>: Monad<µ> {
  fun <A> mzero(): K1<µ, A>
  fun <A> mplus(m: K1<µ, A>): K1<µ, A>
}
