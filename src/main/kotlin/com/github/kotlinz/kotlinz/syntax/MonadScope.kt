package com.github.kotlinz.kotlinz.syntax

import com.github.kotlinz.kotlinz.K1
import kotlin.coroutines.RestrictsSuspension

/**
 * Receiver of the `fx` / `fxReplay` blocks.
 *
 * It is annotated with [RestrictsSuspension], so only [bind] (and other suspend members of this
 * scope) can suspend inside a block; arbitrary suspend functions cannot be called.
 *
 * @see fx
 * @see fxReplay
 */
@RestrictsSuspension
interface MonadScope<T> {
  /** Suspends the block until this monadic value produces a result and returns it (Haskell's `<-`). */
  suspend fun <A> K1<T, A>.bind(): A
}
