package com.github.unhappychoice.kotlinz.type.monad

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.applicative.Applicative

interface Monad<µ>: Applicative<µ> {
  fun <A> join(v: K1<µ, K1<µ, A>>): K1<µ, A>
  fun <A, B> bind(f: (A) -> K1<µ, B>, v: K1<µ, A>): K1<µ, B> = join(fmap(f, v))
}
