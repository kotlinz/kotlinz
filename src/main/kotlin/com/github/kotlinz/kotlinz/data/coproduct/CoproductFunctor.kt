package com.github.kotlinz.kotlinz.data.coproduct

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

interface CoproductFunctor<F, G>: Functor<K2<Coproduct.T, F, G>> {
  val functorF: Functor<F>
  val functorG: Functor<G>

  override fun <A, B> fmap(f: (A) -> B, v: K1<K2<Coproduct.T, F, G>, A>): Coproduct<F, G, B> {
    return Coproduct.narrow(v).fold(
      { fa -> Coproduct.left(functorF.fmap(f, fa)) },
      { ga -> Coproduct.right(functorG.fmap(f, ga)) }
    )
  }
}
