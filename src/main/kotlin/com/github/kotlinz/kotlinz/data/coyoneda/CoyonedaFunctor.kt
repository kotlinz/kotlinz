package com.github.kotlinz.kotlinz.data.coyoneda

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface CoyonedaFunctor<F>: Functor<K1<Coyoneda.T, F>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Coyoneda.T, F>, A>): Coyoneda<F, B> {
    val coyoneda = Coyoneda.narrow(v)
    return Coyoneda.of(coyoneda.fb) { b -> f(coyoneda.f(b)) }
  }
}
