package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

/**
 * A comonad transformer: a comonad `T` built on top of a base comonad `W` that can be lowered back to it.
 *
 * As with [MonadTrans], an instance fixes the base `W` and names the transformed comonad `T` explicitly
 * (e.g. `ComonadTrans<W, K2<EnvT.T, E, W>>`).
 *
 * Laws:
 * - `base.extract(lower(v)) == extract(v)`
 * - `lower(extend({ w -> f(lower(w)) }, v)) == base.extend(f, lower(v))`
 *
 * @see MonadTrans
 */
interface ComonadTrans<W, T>: Comonad<T> {
  /** Drops the transformer's context, returning the underlying base comonad. */
  fun <A> lower(v: K1<T, A>): K1<W, A>
}
