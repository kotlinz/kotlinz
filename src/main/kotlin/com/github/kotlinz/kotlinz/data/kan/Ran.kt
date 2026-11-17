package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

// Right Kan extension: forall B. ((A) -> G<B>) -> H<B>
interface Ran<G, H, A>: K3<Ran.T, G, H, A> {
  class T

  companion object {
    fun <G, H, A> narrow(v: K1<K2<T, G, H>, A>): Ran<G, H, A> = v as Ran<G, H, A>
    fun <G, H> functor(): RanFunctor<G, H> = object: RanFunctor<G, H> {}
  }

  fun <B> runRan(f: (A) -> K1<G, B>): K1<H, B>

  infix fun <B> fmap(f: (A) -> B): Ran<G, H, B> = functor<G, H>().fmap(f, this)
}
