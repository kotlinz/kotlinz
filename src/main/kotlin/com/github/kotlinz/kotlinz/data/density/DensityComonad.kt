package com.github.kotlinz.kotlinz.data.density

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [Density]. Requires no Comonad of F. */
interface DensityComonad<F>: Comonad<K1<Density.T, F>>, DensityCopointed<F>, DensityFunctor<F> {
  override fun <A> duplicate(v: K1<K1<Density.T, F>, A>): Density<F, K1<K1<Density.T, F>, A>> {
    val density = Density.narrow(v)
    return Density.of({ fb: K1<F, Any?> -> Density.of(density.f, fb) }, density.fb)
  }

  override fun <A, B> extend(f: (K1<K1<Density.T, F>, A>) -> B, v: K1<K1<Density.T, F>, A>): Density<F, B> {
    return Density.narrow(fmap(f, duplicate(v)))
  }
}
