package com.github.kotlinz.kotlinz.data.cont

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Cont]. */
interface ContFunctor<R>: Functor<K1<Cont.T, R>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Cont.T, R>, A>): Cont<R, B> {
    return Cont { k -> Cont.narrow(v).run { a -> k(f(a)) } }
  }
}
