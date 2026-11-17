package com.github.kotlinz.kotlinz.data.codensity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface CodensityFunctor<F>: Functor<K1<Codensity.T, F>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Codensity.T, F>, A>): Codensity<F, B> = object: Codensity<F, B>() {
    override fun <R> run(k: (B) -> K1<F, R>): K1<F, R> = Codensity.narrow(v).run { a -> k(f(a)) }
  }
}
