package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

/**
 * A monad with read-only access to an environment of type `R`.
 *
 * Laws:
 * - `local(f, ask()) == fmap(f, ask())`
 * - `local(f, pure(a)) == pure(a)`
 * - `local(g, local(f, m)) == local(f . g, m)`
 *
 * @see MonadState
 * @see MonadWriter
 */
interface MonadReader<T, R>: Monad<T> {
  /** Returns the current environment. */
  fun ask(): K1<T, R>
  /** Runs `v` with the environment modified by `f`. */
  fun <A> local(f: (R) -> R, v: K1<T, A>): K1<T, A>

  /** Returns a projection of the environment. */
  fun <A> reader(f: (R) -> A): K1<T, A> = fmap(f, ask())
}
