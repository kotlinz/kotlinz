package com.github.kotlinz.kotlinz.data.writer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.group.Monoid

interface WriterFunctor<W: Monoid<*, W>> : Functor<K1<Writer.T, W>> {
  val type: Class<W>

  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Writer.T, W>, A>): Writer<W, B> {
    val writer = Writer.narrow(v)
    val (value, w) = writer.run
    return Writer(type, Pair(f(value), w))
  }
}