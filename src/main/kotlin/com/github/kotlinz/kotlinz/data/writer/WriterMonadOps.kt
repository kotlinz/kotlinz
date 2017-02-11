package com.github.kotlinz.kotlinz.data.writer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadOps

interface WriterMonadOps<W: Monoid<*, W>>: MonadOps<K1<Writer.T, W>>, WriterMonad<W> {
  override fun <A, B> liftM(f: (A) -> B): (K1<K1<Writer.T, W>, A>) -> K1<K1<Writer.T, W>, B> {
    return { m ->
      val i = Writer.narrow(m)
      i bind { x -> Writer.pure(type, f(x)) }
    }
  }

  override fun <A, B, C> liftM2(f: (A,  B) -> C): (K1<K1<Writer.T, W>, A>, K1<K1<Writer.T, W>, B>) -> K1<K1<Writer.T, W>, C> {
    return { m1, m2 ->
      val i1 = Writer.narrow(m1)
      val i2 = Writer.narrow(m2)
      i1 bind { x1 -> i2 bind { x2 -> Writer.pure(type, f(x1, x2)) } }
    }
  }

  override fun <A, B, C, D> liftM3(f: (A, B, C) -> D): (K1<K1<Writer.T, W>, A>, K1<K1<Writer.T, W>, B>, K1<K1<Writer.T, W>, C>) -> K1<K1<Writer.T, W>, D> {
    return { m1, m2, m3 ->
      val i1 = Writer.narrow(m1)
      val i2 = Writer.narrow(m2)
      val i3 = Writer.narrow(m3)
      i1 bind { x1 -> i2 bind { x2 -> i3 bind { x3 -> Writer.pure(type, f(x1, x2, x3)) } } }
    }
  }
}
