package com.github.kotlinz.kotlinz.data.compose

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Foldable

interface ComposeFoldable<F, G>: Foldable<K2<Compose.T, F, G>> {
  val foldableF: Foldable<F>
  val foldableG: Foldable<G>

  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<K2<Compose.T, F, G>, A>): B {
    return foldableF.foldl(initial, { acc: B, ga: K1<G, A> -> foldableG.foldl(acc, f, ga) }, Compose.narrow(v).value)
  }

  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<K2<Compose.T, F, G>, A>): B {
    return foldableF.foldr(initial, { ga: K1<G, A>, acc: B -> foldableG.foldr(acc, f, ga) }, Compose.narrow(v).value)
  }
}
