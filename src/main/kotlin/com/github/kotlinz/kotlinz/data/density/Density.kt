package com.github.kotlinz.kotlinz.data.density

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Comonad

// Existentially quantified over the hidden type of `fb`
@Suppress("UNCHECKED_CAST")
class Density<F, A> private constructor(val f: (K1<F, Any?>) -> A, val fb: K1<F, Any?>): K2<Density.T, F, A> {
  class T

  companion object {
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Density<F, A> = v as Density<F, A>

    fun <F, B, A> of(f: (K1<F, B>) -> A, fb: K1<F, B>): Density<F, A> = Density(f as (K1<F, Any?>) -> A, fb as K1<F, Any?>)

    fun <F, A> lift(comonad: Comonad<F>, fa: K1<F, A>): Density<F, A> = of({ w: K1<F, A> -> comonad.extract(w) }, fa)
  }

  fun lower(comonad: Comonad<F>): K1<F, A> = comonad.extend(f, fb)

  // Comonad
  infix fun <B> fmap(g: (A) -> B): Density<F, B> = comonad.fmap(g, this)
  fun extract(): A = comonad.extract(this)
  fun duplicate(): Density<F, K1<K1<T, F>, A>> = comonad.duplicate(this)
  infix fun <B> extend(g: (K1<K1<T, F>, A>) -> B): Density<F, B> = comonad.extend(g, this)
  private val comonad get() = object: DensityComonad<F> {}
}
