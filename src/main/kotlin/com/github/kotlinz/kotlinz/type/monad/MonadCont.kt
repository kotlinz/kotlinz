package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

/**
 * A monad with first-class continuations: [callCC] (call with current continuation) hands the
 * computation an escape function that, when invoked, aborts the rest of the `callCC` block and
 * makes the whole block return its argument.
 *
 * Laws:
 * - Escape: `callCC { k -> bind(f, k(a)) } == pure(a)`
 * - Unused continuation: `callCC { _ -> m } == m`
 * - Return: `callCC { k -> k(a) } == pure(a)`
 *
 * @see Monad
 */
interface MonadCont<T>: Monad<T> {
  /** Runs `f` with an escape function; calling it with `a` makes the whole block return `a`. */
  fun <A, B> callCC(f: ((A) -> K1<T, B>) -> K1<T, A>): K1<T, A>
}
