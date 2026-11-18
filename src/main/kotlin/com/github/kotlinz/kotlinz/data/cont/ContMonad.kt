package com.github.kotlinz.kotlinz.data.cont

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Cont]. Not stack-safe. */
interface ContMonad<R>: Monad<K1<Cont.T, R>>, ContApplicative<R>, ContFunctor<R> {
  override fun <A> join(v: K1<K1<Cont.T, R>, K1<K1<Cont.T, R>, A>>): Cont<R, A> {
    return Cont { k -> Cont.narrow(v).run { inner -> Cont.narrow(inner).run(k) } }
  }

  override fun <A, B> bind(f: (A) -> K1<K1<Cont.T, R>, B>, v: K1<K1<Cont.T, R>, A>): Cont<R, B> {
    return Cont.narrow(super.bind(f, v))
  }
}
