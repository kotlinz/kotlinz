package com.github.kotlinz.kotlinz.data.product

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Product]; maps both components. */
interface ProductFunctor<F, G>: Functor<K2<Product.T, F, G>> {
  val functorF: Functor<F>
  val functorG: Functor<G>

  override fun <A, B> fmap(f: (A) -> B, v: K1<K2<Product.T, F, G>, A>): Product<F, G, B> {
    val product = Product.narrow(v)
    return Product(functorF.fmap(f, product.first), functorG.fmap(f, product.second))
  }
}
