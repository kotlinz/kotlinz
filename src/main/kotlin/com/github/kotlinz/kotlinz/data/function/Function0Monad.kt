package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Function0]. */
interface Function0Monad: Monad<Function0.T>, Function0Applicative, Function0Functor {
  override fun <A> join(v: K1<Function0.T, K1<Function0.T, A>>): Function0<A> {
    return Function0 { Function0.narrow(Function0.narrow(v).run()).run() }
  }

  override fun <A, B> bind(f: (A) -> K1<Function0.T, B>, v: K1<Function0.T, A>): Function0<B> {
    return Function0.narrow(super.bind(f, v))
  }
}
