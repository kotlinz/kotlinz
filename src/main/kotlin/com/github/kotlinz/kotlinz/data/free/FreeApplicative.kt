package com.github.kotlinz.kotlinz.data.free

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface FreeApplicative<F>: Applicative<K1<Free.T, F>> {
  override fun <A> pure(v: A): Free<F, A> = Free.Pure(v)

  override fun <A, B> ap(f: K1<K1<Free.T, F>, (A) -> B>, v: K1<K1<Free.T, F>, A>): Free<F, B> {
    return Free.FlatMap(Free.narrow(f)) { g -> Free.narrow(fmap(g, v)) }
  }
}
