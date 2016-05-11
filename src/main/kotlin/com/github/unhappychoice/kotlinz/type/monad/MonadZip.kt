package com.github.unhappychoice.kotlinz.type.monad

import com.github.unhappychoice.kotlinz.K1

interface MonadZip<µ>: Monad<µ> {
  fun <A, B> mzip(m1: K1<µ, A>, m2: K1<µ, B>): K1<µ, Pair<A, B>>
  fun <A, B, C> mzipWith(m1: K1<µ, A>, m2: K1<µ, B>, f: (A, B) -> C): K1<µ, C>
  fun <A, B> munzip(m : K1<µ, Pair<A, B>>): Pair<K1<µ, A>, K1<µ, B>>
}
