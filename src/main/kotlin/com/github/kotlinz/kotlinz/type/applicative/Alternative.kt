package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe

/**
 * An applicative with a monoid structure on its values: a failing computation and a way to choose.
 *
 * Laws:
 * - Left identity: `orElse(empty(), v) == v`
 * - Right identity: `orElse(v, empty()) == v`
 * - Associativity: `orElse(a, orElse(b, c)) == orElse(orElse(a, b), c)`
 *
 * @see Applicative
 * @see com.github.kotlinz.kotlinz.type.monad.MonadPlus
 */
interface Alternative<T>: Applicative<T> {
  /** The identity of [orElse], typically representing failure or no result. */
  fun <A> empty(): K1<T, A>
  /** Combines two alternatives, typically preferring `v1` and falling back to `v2`. */
  fun <A> orElse(v1: K1<T, A>, v2: K1<T, A>): K1<T, A>

  /** Turns failure of `v` into `Just`/`None` instead of propagating it. */
  fun <A> optional(v: K1<T, A>): K1<T, Maybe<A>> =
    orElse(fmap({ a: A -> Maybe.Just(a) }, v), pure(Maybe.None()))
}
