package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface Function0Applicative: Applicative<Function0.T> {
  override fun <A> pure(v: A): Function0<A> = Function0 { v }

  override fun <A, B> ap(f: K1<Function0.T, (A) -> B>, v: K1<Function0.T, A>): Function0<B> {
    return Function0 { Function0.narrow(f).run()(Function0.narrow(v).run()) }
  }
}
