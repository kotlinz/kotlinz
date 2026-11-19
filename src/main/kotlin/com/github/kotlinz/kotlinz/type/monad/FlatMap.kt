package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Apply

/**
 * An [Apply] whose next computation may depend on the result of the previous one. It is a [Monad]
 * without `pure`.
 *
 * Minimal definition is [join]; [bind] is derived from it but may be overridden for efficiency
 * or stack safety.
 *
 * Laws:
 * - Associativity: `bind(g, bind(f, m)) == bind({ a -> bind(g, f(a)) }, m)`
 * - Consistency with Apply: `ap(mf, m) == bind({ f -> fmap(f, m) }, mf)`
 *
 * @see Monad
 * @see com.github.kotlinz.kotlinz.type.applicative.Apply
 */
interface FlatMap<T>: Apply<T> {
  /** Flattens one level of nesting. */
  fun <A> join(v: K1<T, K1<T, A>>): K1<T, A>
  /** Sequentially composes `v` with `f`, feeding each result of `v` to `f` (a.k.a. flatMap / `>>=`). */
  fun <A, B> bind(f: (A) -> K1<T, B>, v: K1<T, A>): K1<T, B> = join(fmap(f, v))
}
