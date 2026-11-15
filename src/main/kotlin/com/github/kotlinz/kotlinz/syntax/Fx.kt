package com.github.kotlinz.kotlinz.syntax

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/**
 * Do-notation for monads that call the continuation passed to `bind` at most once per run.
 * Each run of the resulting value starts a fresh coroutine, so lazy monads can be run repeatedly.
 */
fun <T, A> Monad<T>.fx(block: suspend MonadScope<T>.() -> A): K1<T, A> {
  return bind({ _: Unit -> FxCoroutine(this, block).run() }, pure(Unit))
}
