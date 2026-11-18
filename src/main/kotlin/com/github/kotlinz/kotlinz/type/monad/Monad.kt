package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/**
 * A monad: an applicative whose next computation may depend on the result of the previous one.
 *
 * Minimal definition is [join]; [bind] is derived from it but may be overridden for efficiency
 * or stack safety. Do-notation is available through `fx` / `fxReplay` in the syntax package.
 *
 * Laws:
 * - Left identity: `bind(f, pure(a)) == f(a)`
 * - Right identity: `bind(::pure, m) == m`
 * - Associativity: `bind(g, bind(f, m)) == bind({ a -> bind(g, f(a)) }, m)`
 *
 * @see com.github.kotlinz.kotlinz.type.applicative.Applicative
 * @see com.github.kotlinz.kotlinz.syntax.fx
 * @see com.github.kotlinz.kotlinz.syntax.fxReplay
 */
interface Monad<T>: Applicative<T> {
  /** Flattens one level of nesting. */
  fun <A> join(v: K1<T, K1<T, A>>): K1<T, A>
  /** Sequentially composes `v` with `f`, feeding each result of `v` to `f` (a.k.a. flatMap / `>>=`). */
  fun <A, B> bind(f: (A) -> K1<T, B>, v: K1<T, A>): K1<T, B> = join(fmap(f, v))
}
