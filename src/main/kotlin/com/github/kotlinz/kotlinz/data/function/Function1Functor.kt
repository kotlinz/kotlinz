package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

interface Function1Functor<R>: Functor<K2<Function1.T, Unit, R>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K2<Function1.T, Unit, R>, A>): Function1<R, B> {
    val function = Function1.narrow(v)
    return Function1 { r -> f(function.run(r)) }
  }
}
