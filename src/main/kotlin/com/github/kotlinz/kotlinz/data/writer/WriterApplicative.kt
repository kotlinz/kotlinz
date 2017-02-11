package com.github.kotlinz.kotlinz.data.writer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

interface WriterApplicative<W: Monoid<*, W>>: Applicative<K1<Writer.T, W>> {
  val type: Class<W>

  override fun <A> pure(v: A): Writer<W, A> = Writer(type, Pair(v, Monoid.mzero(type)))

  override fun <A, B> ap(f: K1<K1<Writer.T, W>, (A) -> B>, v: K1<K1<Writer.T, W>, A>): Writer<W, B> {
    val writer = Writer.narrow(v)
    val writerf = Writer.narrow(f)
    val (value, w) = writerf.run
    val (value2, w2)= Writer.narrow(fmap(value, writer)).run
    return Writer(type, Pair(value2, Monoid.op(type, w, w2)))
  }
}