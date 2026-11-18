package com.github.kotlinz.kotlinz.data.writer

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bifunctor
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Bifunctor instance for [Writer].
 *
 * Maps the log (first parameter) and the value (second parameter) of a Writer.
 * The log function must return a Monoid; its runtime class becomes the new log type.
 */
interface WriterBifunctor : Bifunctor<Writer.T> {
  override fun <A, B, C, D> bimap(f: (A) -> C, g: (B) -> D, v: K2<Writer.T, A, B>): K2<Writer.T, C, D> {
    @Suppress("UNCHECKED_CAST")
    val (value, log) = (v as Writer<*, B>).run as Pair<B, A>
    return rebuild<Nothing, C, D>(f(log), g(value))
  }
}

@Suppress("UNCHECKED_CAST")
private fun <W : Monoid<*, W>, C, D> rebuild(log: C, value: D): K2<Writer.T, C, D> {
  val w = log as W
  return Writer(w.javaClass, Pair(value, w)) as K2<Writer.T, C, D>
}
