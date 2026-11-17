package com.github.kotlinz.kotlinz.data.writer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid
import com.github.kotlinz.kotlinz.type.monad.MonadWriter

interface WriterMonadWriter<W: Monoid<*, W>>: MonadWriter<K1<Writer.T, W>, W>, WriterMonad<W> {
  override fun <A> writer(v: Pair<A, W>): Writer<W, A> = Writer(type, v)

  override fun <A> listen(v: K1<K1<Writer.T, W>, A>): Writer<W, Pair<A, W>> {
    val (a, w) = Writer.narrow(v).run
    return Writer(type, Pair(Pair(a, w), w))
  }

  override fun <A> pass(v: K1<K1<Writer.T, W>, Pair<A, (W) -> W>>): Writer<W, A> {
    val (pair, w) = Writer.narrow(v).run
    return Writer(type, Pair(pair.first, pair.second(w)))
  }

  override fun tell(w: W): Writer<W, Unit> = writer(Pair(Unit, w))
}
