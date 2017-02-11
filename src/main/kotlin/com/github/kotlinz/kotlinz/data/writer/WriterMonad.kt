package com.github.kotlinz.kotlinz.data.writer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.Monad

interface WriterMonad<W: Monoid<*, W>>: Monad<K1<Writer.T, W>>, WriterApplicative<W>, WriterFunctor<W> {
  override fun <A> join(v: K1<K1<Writer.T, W>, K1<K1<Writer.T, W>, A>>): Writer<W, A> {
    val writer = Writer.narrow(v)
    val (value, w) = writer.run
    val (value2, w2) = Writer.narrow(value).run
    return Writer(type, Pair(value2, Monoid.op(type, w, w2)))
  }

  override fun <A, B> bind(f: (A) -> K1<K1<Writer.T, W>, B>, v: K1<K1<Writer.T, W>, A>): Writer<W, B> {
    return Writer.narrow(super.bind(f, v))
  }
}