package com.github.kotlinz.kotlinz.data.writer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps
import com.github.kotlinz.kotlinz.type.group.Monoid

interface WriterApplicativeOps<W: Monoid<*, W>>: ApplicativeOps<K1<Writer.T, W>>, WriterMonad<W> {
  override fun <A, B> liftA(f: (A) -> B): (K1<K1<Writer.T, W>, A>) -> Writer<W, B> {
    return { a -> Writer.narrow(a) ap Writer.pure(type, f) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<K1<Writer.T, W>, A>, K1<K1<Writer.T, W>, B>) -> Writer<W, C> {
    return { a1, a2 ->
      val i1 = Writer.narrow(a1)
      val i2 = Writer.narrow(a2)
      i2 ap (i1 fmap f.curried())
    }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<K1<Writer.T, W>, A>, K1<K1<Writer.T, W>, B>, K1<K1<Writer.T, W>, C>) -> Writer<W, D> {
    return { a1, a2, a3 ->
      val i1 = Writer.narrow(a1)
      val i2 = Writer.narrow(a2)
      val i3 = Writer.narrow(a3)
      i3 ap (i2 ap (i1 fmap f.curried()))
    }
  }
} 
