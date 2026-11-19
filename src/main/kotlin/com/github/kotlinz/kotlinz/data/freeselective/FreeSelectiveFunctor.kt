package com.github.kotlinz.kotlinz.data.freeselective

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [FreeSelective]. */
interface FreeSelectiveFunctor<F>: Functor<K1<FreeSelective.T, F>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<FreeSelective.T, F>, A>): FreeSelective<F, B> = FreeSelective.narrow(v) fmap f
}
