package com.github.kotlinz.kotlinz.data.reader

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadOps

interface ReaderMonadOps<S>: MonadOps<K1<Reader.T, S>>, ReaderMonad<S> {
  override fun <A, B> liftM(f: (A) -> B): (K1<K1<Reader.T, S>, A>) -> K1<K1<Reader.T, S>, B> {
    return { m ->
      val i = Reader.narrow(m)
      i bind { x -> Reader.pure<S, B>(f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A,  B) -> C): (K1<K1<Reader.T, S>, A>, K1<K1<Reader.T, S>, B>) -> K1<K1<Reader.T, S>, C> {
    return { m1, m2 ->
      val i1 = Reader.narrow(m1)
      val i2 = Reader.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> Reader.pure<S, C>(f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<K1<Reader.T, S>, A>, K1<K1<Reader.T, S>, B>, K1<K1<Reader.T, S>, C>) -> K1<K1<Reader.T, S>, D> {
    return { m1, m2, m3 ->
      val i1 = Reader.narrow(m1)
      val i2 = Reader.narrow(m2)
      val i3 = Reader.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> Reader.pure<S, D>(f(x1, x2, x3)) } } }
    }
  }
}