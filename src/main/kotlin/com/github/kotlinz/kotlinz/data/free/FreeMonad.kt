package com.github.kotlinz.kotlinz.data.free

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Free]. */
interface FreeMonad<F>: Monad<K1<Free.T, F>>, FreeApplicative<F>, FreeFunctor<F> {
  override fun <A> join(v: K1<K1<Free.T, F>, K1<K1<Free.T, F>, A>>): Free<F, A> {
    return Free.FlatMap(Free.narrow(v)) { inner -> Free.narrow(inner) }
  }

  override fun <A, B> bind(f: (A) -> K1<K1<Free.T, F>, B>, v: K1<K1<Free.T, F>, A>): Free<F, B> {
    return Free.FlatMap(Free.narrow(v)) { a -> Free.narrow(f(a)) }
  }
}
