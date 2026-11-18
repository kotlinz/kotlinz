package com.github.kotlinz.kotlinz.data.free

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Free]; does not require `F` to be a functor. */
interface FreeFunctor<F>: Functor<K1<Free.T, F>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Free.T, F>, A>): Free<F, B> {
    return Free.FlatMap(Free.narrow(v)) { a -> Free.Pure(f(a)) }
  }
}
