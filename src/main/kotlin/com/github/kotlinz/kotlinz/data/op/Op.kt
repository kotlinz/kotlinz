package com.github.kotlinz.kotlinz.data.op

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2

class Op<R, A>(val run: (A) -> R): K2<Op.T, R, A> {
  class T

  companion object {
    fun <R, A> narrow(v: K1<K1<T, R>, A>): Op<R, A> = v as Op<R, A>
  }

  // Contravariant
  infix fun <B> contramap(f: (B) -> A): Op<R, B> = contravariant.contramap(f, this)
  private val contravariant = object: OpContravariant<R> {}
}
