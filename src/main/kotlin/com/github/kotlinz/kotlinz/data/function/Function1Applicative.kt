package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Function1] with a fixed input type [R]; both functions receive the same input. */
interface Function1Applicative<R>: Applicative<K2<Function1.T, Unit, R>> {
  override fun <A> pure(v: A): Function1<R, A> = Function1 { v }

  override fun <A, B> ap(f: K1<K2<Function1.T, Unit, R>, (A) -> B>, v: K1<K2<Function1.T, Unit, R>, A>): Function1<R, B> {
    val functionf = Function1.narrow(f)
    val function = Function1.narrow(v)
    return Function1 { r -> functionf.run(r)(function.run(r)) }
  }
}
