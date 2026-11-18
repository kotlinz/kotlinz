package com.github.kotlinz.kotlinz.syntax

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/**
 * Do-notation for any monad, including those that call the continuation passed to `bind` several times.
 * The block is re-run from the start for every bound value, so code between binds may run multiple times.
 *
 * Each `bind` callback starts a new coroutine that replays the values bound so far and then
 * continues past the current bind, emulating a multi-shot continuation:
 *
 * ```kotlin
 * (object : ListKMonad {}).fxReplay { listKOf(1, 2).bind() * listKOf(3, 4).bind() } // ListK(3, 4, 6, 8)
 * ```
 *
 * Caveats: the block must be deterministic and free of side effects outside the monad, since
 * side effects between binds are repeated, and the prefix of the block is re-executed for every bound value.
 * Prefer [fx] for single-shot monads.
 *
 * @see fx
 * @see MonadScope
 */
fun <T, A> Monad<T>.fxReplay(block: suspend MonadScope<T>.() -> A): K1<T, A> {
  return bind({ _: Unit -> ReplayCoroutine(this, block, emptyList()).run() }, pure(Unit))
}
