package com.github.kotlinz.kotlinz.data.coproduct

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Foldable

/** Foldable instance for [Coproduct]. Requires Foldables of F and G. */
interface CoproductFoldable<F, G>: Foldable<K2<Coproduct.T, F, G>> {
  /** Foldable of the left side. */
  val foldableF: Foldable<F>
  /** Foldable of the right side. */
  val foldableG: Foldable<G>

  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<K2<Coproduct.T, F, G>, A>): B {
    return Coproduct.narrow(v).fold({ fa -> foldableF.foldl(initial, f, fa) }, { ga -> foldableG.foldl(initial, f, ga) })
  }

  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<K2<Coproduct.T, F, G>, A>): B {
    return Coproduct.narrow(v).fold({ fa -> foldableF.foldr(initial, f, fa) }, { ga -> foldableG.foldr(initial, f, ga) })
  }
}
