package com.github.kotlinz.kotlinz.data.coproduct

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Coproduct]. Requires Functors of F and G. */
interface CoproductFunctor<F, G>: Functor<K2<Coproduct.T, F, G>> {
  /** Functor of the left side. */
  val functorF: Functor<F>
  /** Functor of the right side. */
  val functorG: Functor<G>

  override fun <A, B> fmap(f: (A) -> B, v: K1<K2<Coproduct.T, F, G>, A>): Coproduct<F, G, B> {
    return Coproduct.narrow(v).fold(
      { fa -> Coproduct.left(functorF.fmap(f, fa)) },
      { ga -> Coproduct.right(functorG.fmap(f, ga)) }
    )
  }
}
