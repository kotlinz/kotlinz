package com.github.kotlinz.kotlinz.data.freer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Freer]; does not require `F` to be a functor. */
interface FreerFunctor<F>: Functor<K1<Freer.T, F>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Freer.T, F>, A>): Freer<F, B> =
    Freer.narrow(v) bind { a -> Freer.Pure(f(a)) }
}
