package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

/**
 * A monad transformer: a monad `T` built on top of a base monad `F` into which base computations can be lifted.
 *
 * Kotlin's kind encoding cannot abstract over "a transformer applied to any base", so an instance fixes the
 * base `F` and names the transformed monad `T` explicitly (e.g. `MonadTrans<F, K1<MaybeT.T, F>>`).
 *
 * Laws:
 * - `lift(base.pure(a)) == pure(a)`
 * - `lift(base.bind(f, m)) == bind({ a -> lift(f(a)) }, lift(m))`
 *
 * @see MFunctor
 */
interface MonadTrans<F, T>: Monad<T> {
  /** Embeds a computation of the base monad into the transformed monad. */
  fun <A> lift(v: K1<F, A>): K1<T, A>
}
