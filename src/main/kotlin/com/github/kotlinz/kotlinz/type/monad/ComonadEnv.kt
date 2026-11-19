package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

/**
 * A comonad carrying a read-only environment `E` alongside the focused value (the class of `Env`-like comonads).
 *
 * Laws:
 * - `ask(fmap(f, v)) == ask(v)`
 * - `ask(extend(f, v)) == ask(v)`
 *
 * @see MonadReader
 * @see ComonadStore
 */
interface ComonadEnv<T, E>: Comonad<T> {
  /** Returns the environment. */
  fun <A> ask(v: K1<T, A>): E

  /** Returns the environment projected through `f`. */
  fun <A, B> asks(f: (E) -> B, v: K1<T, A>): B = f(ask(v))
}
