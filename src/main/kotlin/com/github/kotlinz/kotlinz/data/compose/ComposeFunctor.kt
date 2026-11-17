package com.github.kotlinz.kotlinz.data.compose

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

interface ComposeFunctor<F, G>: Functor<K2<Compose.T, F, G>> {
  val functorF: Functor<F>
  val functorG: Functor<G>

  override fun <A, B> fmap(f: (A) -> B, v: K1<K2<Compose.T, F, G>, A>): Compose<F, G, B> {
    return Compose(functorF.fmap({ ga: K1<G, A> -> functorG.fmap(f, ga) }, Compose.narrow(v).value))
  }
}
