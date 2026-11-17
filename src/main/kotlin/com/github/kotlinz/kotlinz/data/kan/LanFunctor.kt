package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

interface LanFunctor<G, H>: Functor<K2<Lan.T, G, H>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K2<Lan.T, G, H>, A>): Lan<G, H, B> {
    val lan = Lan.narrow(v)
    return Lan({ gb: K1<G, Any?> -> f(lan.lower(gb)) }, lan.value)
  }
}
