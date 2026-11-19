package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/**
 * A functor that can apply a function in a context to a value in a context, combining independent
 * effects. It is an [Applicative] without `pure`, so it also fits structures that have no empty
 * or minimal value (e.g. non-empty collections or tuples over a semigroup).
 *
 * Laws:
 * - Associative composition: `ap(ap(fmap(compose, u), v), w) == ap(u, ap(v, w))`
 *
 * @see Applicative
 * @see com.github.kotlinz.kotlinz.type.monad.FlatMap
 */
interface Apply<T>: Functor<T> {
  /** Applies the function(s) in `f` to the value(s) in `v`, combining both effects. */
  fun <A, B> ap(f: K1<T, (A) -> B>, v: K1<T, A>): K1<T, B>
}
