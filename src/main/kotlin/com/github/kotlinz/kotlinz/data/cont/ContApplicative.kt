package com.github.kotlinz.kotlinz.data.cont

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface ContApplicative<R>: Applicative<K1<Cont.T, R>>, ContFunctor<R> {
  override fun <A> pure(v: A): Cont<R, A> = Cont { k -> k(v) }

  override fun <A, B> ap(f: K1<K1<Cont.T, R>, (A) -> B>, v: K1<K1<Cont.T, R>, A>): Cont<R, B> {
    return Cont { k -> Cont.narrow(f).run { g -> Cont.narrow(fmap(g, v)).run(k) } }
  }
}
