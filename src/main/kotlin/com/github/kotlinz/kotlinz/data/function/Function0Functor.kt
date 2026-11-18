package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Function0]. */
interface Function0Functor: Functor<Function0.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Function0.T, A>): Function0<B> {
    return Function0 { f(Function0.narrow(v).run()) }
  }
}
