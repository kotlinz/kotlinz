package com.github.kotlinz.kotlinz.data.product

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface ProductApplicative<F, G>: Applicative<K2<Product.T, F, G>>, ProductFunctor<F, G> {
  override val functorF: Applicative<F>
  override val functorG: Applicative<G>

  override fun <A> pure(v: A): Product<F, G, A> = Product(functorF.pure(v), functorG.pure(v))

  override fun <A, B> ap(f: K1<K2<Product.T, F, G>, (A) -> B>, v: K1<K2<Product.T, F, G>, A>): Product<F, G, B> {
    val productf = Product.narrow(f)
    val product = Product.narrow(v)
    return Product(functorF.ap(productf.first, product.first), functorG.ap(productf.second, product.second))
  }
}
