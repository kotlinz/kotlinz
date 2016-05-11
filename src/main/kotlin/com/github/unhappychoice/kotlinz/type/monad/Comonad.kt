package com.github.unhappychoice.kotlinz.type.monad

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.Functor
import com.github.unhappychoice.kotlinz.type.pointed.Copointed

interface Comonad<µ>: Copointed<µ>, Functor<µ> {
  fun <A> duplicate(v: K1<µ, A>): K1<µ, K1<µ, A>>
  fun <A, B> extend(f: (K1<µ, A>) -> B, v: K1<µ, A>): K1<µ, B> = fmap(f, duplicate(v))
}