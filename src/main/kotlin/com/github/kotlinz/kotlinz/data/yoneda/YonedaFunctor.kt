package com.github.kotlinz.kotlinz.data.yoneda

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Yoneda]; works for any `F` and fuses maps. */
interface YonedaFunctor<F>: Functor<K1<Yoneda.T, F>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Yoneda.T, F>, A>): Yoneda<F, B> = object: Yoneda<F, B>() {
    override fun <C> run(g: (B) -> C): K1<F, C> = Yoneda.narrow(v).run { a -> g(f(a)) }
  }
}
