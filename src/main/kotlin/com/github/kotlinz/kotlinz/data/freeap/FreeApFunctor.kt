package com.github.kotlinz.kotlinz.data.freeap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface FreeApFunctor<F>: Functor<K1<FreeAp.T, F>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<FreeAp.T, F>, A>): FreeAp<F, B> = FreeAp.narrow(v) fmap f
}
