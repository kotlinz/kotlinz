package com.github.kotlinz.kotlinz.data.freeap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [FreeAp]; effects of the function argument run first. */
interface FreeApApplicative<F>: Applicative<K1<FreeAp.T, F>>, FreeApFunctor<F> {
  override fun <A> pure(v: A): FreeAp<F, A> = FreeAp.Pure(v)

  override fun <A, B> ap(f: K1<K1<FreeAp.T, F>, (A) -> B>, v: K1<K1<FreeAp.T, F>, A>): FreeAp<F, B> =
    FreeAp.narrow(v) ap FreeAp.narrow(f)
}
