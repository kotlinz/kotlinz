package com.github.unhappychoice.kotlinz.type.applicative

import com.github.unhappychoice.kotlinz.K1

interface ApplicativeOps<µ>: Applicative<µ> {
  fun <A, B> liftA(f: (A) -> B): (K1<µ, A>) -> K1<µ, B>
  fun <A, B, C> liftA2(f: (A, B) -> C): (K1<µ, A>, K1<µ, B>) -> K1<µ, C>
  fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<µ, A>, K1<µ, B>, K1<µ, C>) -> K1<µ, D>
}