package com.github.kotlinz.kotlinz.syntax

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/**
 * Do-notation for any monad, including those that call the continuation passed to `bind` several times.
 * The block is re-run from the start for every bound value, so code between binds may run multiple times.
 */
fun <T, A> Monad<T>.fxReplay(block: suspend MonadScope<T>.() -> A): K1<T, A> {
  return bind({ _: Unit -> ReplayCoroutine(this, block, emptyList()).run() }, pure(Unit))
}
