package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

/**
 * A comonad that can be queried at a monoidal offset `M` from the focus (the class of `Traced`-like comonads).
 *
 * Laws:
 * - `trace(mzero, v) == extract(v)`
 * - `trace(m2, extend({ w -> trace(m1, w) }, v)) == trace(op(m2, m1), v)`
 *
 * @see MonadWriter
 * @see ComonadStore
 */
interface ComonadTraced<T, M>: Comonad<T> {
  /** Returns the value at offset `m` from the focus. */
  fun <A> trace(m: M, v: K1<T, A>): A

  /** Returns the value at an offset derived from the focused value. */
  fun <A> traces(f: (A) -> M, v: K1<T, A>): A = trace(f(extract(v)), v)
}
