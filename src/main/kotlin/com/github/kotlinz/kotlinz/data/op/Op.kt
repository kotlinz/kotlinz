package com.github.kotlinz.kotlinz.data.op

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2

/**
 * A function `(A) -> R` viewed as contravariant in its argument `A`.
 *
 * Use it to adapt a consumer of `A` to a consumer of `B` with [contramap].
 * Has instances for Contravariant and, when `R` is a Monoid, Divisible and Decidable ([OpDecidable]).
 *
 * ```kotlin
 * val show = Op { a: Int -> a.toString() }
 * (show contramap { s: String -> s.length }).run("abc") // "3"
 * ```
 */
class Op<R, A>(val run: (A) -> R): K2<Op.T, R, A> {
  /** Higher-kinded witness tag for [Op]. */
  class T

  companion object {
    /** Downcasts a higher-kinded value back to [Op]. */
    fun <R, A> narrow(v: K1<K1<T, R>, A>): Op<R, A> = v as Op<R, A>
  }

  // Contravariant
  /** Pre-composes [f] with this function. */
  infix fun <B> contramap(f: (B) -> A): Op<R, B> = contravariant.contramap(f, this)
  private val contravariant = object: OpContravariant<R> {}
}
