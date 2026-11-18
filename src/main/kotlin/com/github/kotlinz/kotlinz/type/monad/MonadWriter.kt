package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

/**
 * A monad that accumulates an output log of type `W` (usually a monoid) alongside its result.
 *
 * Laws:
 * - `bind({ tell(w2) }, tell(w1)) == tell(w1 + w2)`
 * - `listen(pure(a)) == pure(Pair(a, mzero))`
 * - `listen(tell(w)) == fmap({ Pair(Unit, w) }, tell(w))`
 *
 * @see MonadReader
 * @see MonadState
 */
interface MonadWriter<T, W>: Monad<T> {
  /** Creates a computation that returns the first component and logs the second. */
  fun <A> writer(v: Pair<A, W>): K1<T, A>
  /** Runs `v` and additionally returns the output it produced. */
  fun <A> listen(v: K1<T, A>): K1<T, Pair<A, W>>
  /** Runs `v` and applies the returned function to its output. */
  fun <A> pass(v: K1<T, Pair<A, (W) -> W>>): K1<T, A>

  /** Appends `w` to the output. */
  fun tell(w: W): K1<T, Unit> = writer(Pair(Unit, w))

  /** Like [listen], but returns a projection of the output. */
  fun <A, B> listens(f: (W) -> B, v: K1<T, A>): K1<T, Pair<A, B>> {
    return fmap({ (a, w) -> Pair(a, f(w)) }, listen(v))
  }

  /** Runs `v` and transforms its output with `f`. */
  fun <A> censor(f: (W) -> W, v: K1<T, A>): K1<T, A> = pass(fmap({ a -> Pair(a, f) }, v))
}
