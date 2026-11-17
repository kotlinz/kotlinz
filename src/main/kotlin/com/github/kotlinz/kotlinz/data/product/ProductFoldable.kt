package com.github.kotlinz.kotlinz.data.product

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Foldable

interface ProductFoldable<F, G>: Foldable<K2<Product.T, F, G>> {
  val foldableF: Foldable<F>
  val foldableG: Foldable<G>

  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<K2<Product.T, F, G>, A>): B {
    val product = Product.narrow(v)
    return foldableG.foldl(foldableF.foldl(initial, f, product.first), f, product.second)
  }

  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<K2<Product.T, F, G>, A>): B {
    val product = Product.narrow(v)
    return foldableF.foldr(foldableG.foldr(initial, f, product.second), f, product.first)
  }
}
