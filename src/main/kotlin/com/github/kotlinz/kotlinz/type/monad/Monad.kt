package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.type.applicative.Applicative

/**
 * A monad: a [FlatMap] that is also an [Applicative], so the next computation may depend on the
 * result of the previous one and plain values can be lifted with `pure`.
 *
 * Minimal definition is [FlatMap.join]; [FlatMap.bind] is derived from it but may be overridden for
 * efficiency or stack safety. Do-notation is available through `fx` / `fxReplay` in the syntax package.
 *
 * Laws:
 * - Left identity: `bind(f, pure(a)) == f(a)`
 * - Right identity: `bind(::pure, m) == m`
 * - Associativity: `bind(g, bind(f, m)) == bind({ a -> bind(g, f(a)) }, m)`
 *
 * @see FlatMap
 * @see com.github.kotlinz.kotlinz.type.applicative.Applicative
 * @see com.github.kotlinz.kotlinz.syntax.fx
 * @see com.github.kotlinz.kotlinz.syntax.fxReplay
 */
interface Monad<T>: FlatMap<T>, Applicative<T>
