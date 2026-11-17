package com.github.kotlinz.kotlinz.data.compose

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface ComposeApplicative<F, G>: Applicative<K2<Compose.T, F, G>>, ComposeFunctor<F, G> {
  override val functorF: Applicative<F>
  override val functorG: Applicative<G>

  override fun <A> pure(v: A): Compose<F, G, A> = Compose(functorF.pure(functorG.pure(v)))

  override fun <A, B> ap(f: K1<K2<Compose.T, F, G>, (A) -> B>, v: K1<K2<Compose.T, F, G>, A>): Compose<F, G, B> {
    val apG = { gf: K1<G, (A) -> B> -> { ga: K1<G, A> -> functorG.ap(gf, ga) } }
    return Compose(functorF.ap(functorF.fmap(apG, Compose.narrow(f).value), Compose.narrow(v).value))
  }
}
