package com.github.kotlinz.kotlinz.data.density

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface DensityFunctor<F>: Functor<K1<Density.T, F>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Density.T, F>, A>): Density<F, B> {
    val density = Density.narrow(v)
    return Density.of({ fb: K1<F, Any?> -> f(density.f(fb)) }, density.fb)
  }
}
