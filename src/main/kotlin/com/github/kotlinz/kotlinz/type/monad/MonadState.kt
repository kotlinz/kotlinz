package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

/**
 * A monad with a mutable-looking state of type `S` threaded through the computation.
 *
 * Laws:
 * - `bind({ s -> put(s) }, get()) == pure(Unit)`
 * - `bind({ get() }, put(s)) == fmap({ s }, put(s))`
 * - `bind({ put(s2) }, put(s1)) == put(s2)`
 *
 * @see MonadReader
 * @see MonadWriter
 */
interface MonadState<T, S>: Monad<T> {
  /** Returns the current state. */
  fun get(): K1<T, S>
  /** Replaces the state with `s`. */
  fun put(s: S): K1<T, Unit>

  /** Updates the state with `f`. */
  fun modify(f: (S) -> S): K1<T, Unit> = bind({ s -> put(f(s)) }, get())
  /** Returns a projection of the state. */
  fun <A> gets(f: (S) -> A): K1<T, A> = fmap(f, get())

  /** Runs a pure state transition, returning its result and storing the new state. */
  fun <A> state(f: (S) -> Pair<A, S>): K1<T, A> {
    return bind({ s -> f(s).let { (a, s2) -> fmap({ _: Unit -> a }, put(s2)) } }, get())
  }
}
