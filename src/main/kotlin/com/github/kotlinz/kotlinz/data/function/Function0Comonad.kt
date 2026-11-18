package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [Function0]. */
interface Function0Comonad: Comonad<Function0.T>, Function0Copointed, Function0Functor {
  override fun <A> duplicate(v: K1<Function0.T, A>): Function0<K1<Function0.T, A>> = Function0 { v }

  override fun <A, B> extend(f: (K1<Function0.T, A>) -> B, v: K1<Function0.T, A>): Function0<B> {
    return fmap(f, duplicate(v))
  }
}
