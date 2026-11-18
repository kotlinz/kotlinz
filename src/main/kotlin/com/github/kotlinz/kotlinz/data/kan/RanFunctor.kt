package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Ran]. */
interface RanFunctor<G, H>: Functor<K2<Ran.T, G, H>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K2<Ran.T, G, H>, A>): Ran<G, H, B> {
    val ran = Ran.narrow(v)
    return object: Ran<G, H, B> {
      override fun <C> runRan(f2: (B) -> K1<G, C>): K1<H, C> = ran.runRan { a -> f2(f(a)) }
    }
  }
}
