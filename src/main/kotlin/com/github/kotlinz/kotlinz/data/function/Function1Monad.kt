package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Monad

interface Function1Monad<R>: Monad<K2<Function1.T, Unit, R>>, Function1Applicative<R>, Function1Functor<R> {
  override fun <A> join(v: K1<K2<Function1.T, Unit, R>, K1<K2<Function1.T, Unit, R>, A>>): Function1<R, A> {
    val function = Function1.narrow(v)
    return Function1 { r -> Function1.narrow(function.run(r)).run(r) }
  }

  override fun <A, B> bind(f: (A) -> K1<K2<Function1.T, Unit, R>, B>, v: K1<K2<Function1.T, Unit, R>, A>): Function1<R, B> {
    return Function1.narrow(super.bind(f, v))
  }
}
