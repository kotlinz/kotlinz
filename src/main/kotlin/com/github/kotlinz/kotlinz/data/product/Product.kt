package com.github.kotlinz.kotlinz.data.product

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.Foldable
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.applicative.Applicative

data class Product<F, G, A>(val first: K1<F, A>, val second: K1<G, A>): K3<Product.T, F, G, A> {
  class T

  companion object {
    fun <F, G, A> narrow(v: K1<K2<T, F, G>, A>): Product<F, G, A> = v as Product<F, G, A>

    // Instances
    fun <F, G> functor(functorF: Functor<F>, functorG: Functor<G>): ProductFunctor<F, G> =
      object: ProductFunctor<F, G> { override val functorF = functorF; override val functorG = functorG }
    fun <F, G> applicative(functorF: Applicative<F>, functorG: Applicative<G>): ProductApplicative<F, G> = applicativeOps(functorF, functorG)
    fun <F, G> applicativeOps(functorF: Applicative<F>, functorG: Applicative<G>): ProductApplicativeOps<F, G> =
      object: ProductApplicativeOps<F, G> { override val functorF = functorF; override val functorG = functorG }
    fun <F, G> foldable(foldableF: Foldable<F>, foldableG: Foldable<G>): ProductFoldable<F, G> =
      object: ProductFoldable<F, G> { override val foldableF = foldableF; override val foldableG = foldableG }

    // Applicative
    fun <F, G, A> pure(functorF: Applicative<F>, functorG: Applicative<G>, v: A): Product<F, G, A> = applicative(functorF, functorG).pure(v)
  }
}
