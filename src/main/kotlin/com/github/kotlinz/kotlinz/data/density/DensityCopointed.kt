package com.github.kotlinz.kotlinz.data.density

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.pointed.Copointed

/** Copointed instance for [Density]. */
interface DensityCopointed<F>: Copointed<K1<Density.T, F>> {
  override fun <A> extract(v: K1<K1<Density.T, F>, A>): A {
    val density = Density.narrow(v)
    return density.f(density.fb)
  }
}
