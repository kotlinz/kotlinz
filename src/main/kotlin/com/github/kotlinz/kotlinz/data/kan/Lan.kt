package com.github.kotlinz.kotlinz.data.kan

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3

// Left Kan extension: exists B. ((G<B>) -> A, H<B>). The existential type is erased internally.
@Suppress("UNCHECKED_CAST")
class Lan<G, H, A> private constructor(
  internal val lower: (K1<G, Any?>) -> A,
  internal val value: K1<H, Any?>
): K3<Lan.T, G, H, A> {
  class T

  interface Fold<G, H, A, R> {
    operator fun <B> invoke(f: (K1<G, B>) -> A, v: K1<H, B>): R
  }

  companion object {
    fun <G, H, A> narrow(v: K1<K2<T, G, H>, A>): Lan<G, H, A> = v as Lan<G, H, A>
    fun <G, H> functor(): LanFunctor<G, H> = object: LanFunctor<G, H> {}

    operator fun <G, H, B, A> invoke(f: (K1<G, B>) -> A, v: K1<H, B>): Lan<G, H, A> =
      Lan(lower = { gb -> f(gb as K1<G, B>) }, value = v as K1<H, Any?>)
  }

  fun <R> fold(f: Fold<G, H, A, R>): R = f(lower, value)

  infix fun <B> fmap(f: (A) -> B): Lan<G, H, B> = functor<G, H>().fmap(f, this)
}
