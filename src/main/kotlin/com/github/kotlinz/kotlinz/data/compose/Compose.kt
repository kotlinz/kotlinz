package com.github.kotlinz.kotlinz.data.compose

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.Foldable
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.applicative.Applicative

data class Compose<F, G, A>(val value: K1<F, K1<G, A>>): K3<Compose.T, F, G, A> {
  class T

  companion object {
    fun <F, G, A> narrow(v: K1<K2<T, F, G>, A>): Compose<F, G, A> = v as Compose<F, G, A>

    // Instances
    fun <F, G> functor(functorF: Functor<F>, functorG: Functor<G>): ComposeFunctor<F, G> =
      object: ComposeFunctor<F, G> { override val functorF = functorF; override val functorG = functorG }
    fun <F, G> applicative(functorF: Applicative<F>, functorG: Applicative<G>): ComposeApplicative<F, G> = applicativeOps(functorF, functorG)
    fun <F, G> applicativeOps(functorF: Applicative<F>, functorG: Applicative<G>): ComposeApplicativeOps<F, G> =
      object: ComposeApplicativeOps<F, G> { override val functorF = functorF; override val functorG = functorG }
    fun <F, G> foldable(foldableF: Foldable<F>, foldableG: Foldable<G>): ComposeFoldable<F, G> =
      object: ComposeFoldable<F, G> { override val foldableF = foldableF; override val foldableG = foldableG }

    // Applicative
    fun <F, G, A> pure(functorF: Applicative<F>, functorG: Applicative<G>, v: A): Compose<F, G, A> = applicative(functorF, functorG).pure(v)
  }
}
