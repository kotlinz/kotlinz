package com.github.unhappychoice.kotlinz.type.applicative

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.Functor
import com.github.unhappychoice.kotlinz.type.pointed.Pointed

interface Applicative<µ>: Functor<µ>, Pointed<µ> {
  fun <A, B> ap(f: K1<µ, (A) -> B>, v: K1<µ, A>): K1<µ, B>
}