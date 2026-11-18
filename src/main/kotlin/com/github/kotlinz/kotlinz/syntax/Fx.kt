package com.github.kotlinz.kotlinz.syntax

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/**
 * Do-notation for monads that call the continuation passed to `bind` at most once per run.
 * Each run of the resulting value starts a fresh coroutine, so lazy monads can be run repeatedly.
 *
 * Inside the block, `m.bind()` suspends until `m` yields a value, and the block's return value is
 * wrapped with `pure`. The block is translated into nested `bind` calls on a single coroutine:
 *
 * ```kotlin
 * Maybe.fx { val a = Maybe.Just(1).bind(); val b = Maybe.Just(2).bind(); a + b } // Just(3)
 * ```
 *
 * Single-shot only: a Kotlin continuation can be resumed at most once, so monads that invoke the
 * `bind` callback several times (e.g. `ListK`, `NonEmptyList`) must not be used with `fx`; they
 * would resume the same continuation again and produce wrong results or fail. Use [fxReplay] for those. Monads that never invoke it (e.g. `None`, `Left`) short-circuit.
 *
 * @see fxReplay
 * @see MonadScope
 */
fun <T, A> Monad<T>.fx(block: suspend MonadScope<T>.() -> A): K1<T, A> {
  return bind({ _: Unit -> FxCoroutine(this, block).run() }, pure(Unit))
}
