package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/**
 * A comonad with a current position `S` from which values at other positions can be looked up
 * (the class of `Store`-like comonads).
 *
 * Laws:
 * - `peek(pos(v), v) == extract(v)`
 * - `pos(seek(s, v)) == s`
 * - `seek(pos(v), v) == v`
 *
 * @see ComonadEnv
 * @see ComonadTraced
 */
interface ComonadStore<T, S>: Comonad<T> {
  /** Returns the current position. */
  fun <A> pos(v: K1<T, A>): S
  /** Looks up the value at the absolute position `s`. */
  fun <A> peek(s: S, v: K1<T, A>): A

  /** Looks up the value at a position relative to the current one. */
  fun <A> peeks(f: (S) -> S, v: K1<T, A>): A = peek(f(pos(v)), v)
  /** Moves to the absolute position `s`. */
  fun <A> seek(s: S, v: K1<T, A>): K1<T, A> = peek(s, duplicate(v))
  /** Moves to a position relative to the current one. */
  fun <A> seeks(f: (S) -> S, v: K1<T, A>): K1<T, A> = peeks(f, duplicate(v))
  /** Looks up values at every position produced by `f`, inside functor `F`. */
  fun <F, A> experiment(functor: Functor<F>, f: (S) -> K1<F, S>, v: K1<T, A>): K1<F, A> =
    functor.fmap({ s: S -> peek(s, v) }, f(pos(v)))
}
