package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.pointed.Pointed

/**
 * An applicative functor: a functor that can apply a function in a context to a value in a context,
 * combining independent effects.
 *
 * Laws:
 * - Identity: `ap(pure(id), v) == v`
 * - Homomorphism: `ap(pure(f), pure(x)) == pure(f(x))`
 * - Interchange: `ap(u, pure(y)) == ap(pure { f -> f(y) }, u)`
 * - Composition: `ap(ap(ap(pure(compose), u), v), w) == ap(u, ap(v, w))`
 * - Consistency with Functor: `fmap(f, v) == ap(pure(f), v)`
 *
 * @see com.github.kotlinz.kotlinz.type.Functor
 * @see com.github.kotlinz.kotlinz.type.monad.Monad
 */
interface Applicative<T>: Functor<T>, Pointed<T> {
  /** Applies the function(s) in `f` to the value(s) in `v`, combining both effects. */
  fun <A, B> ap(f: K1<T, (A) -> B>, v: K1<T, A>): K1<T, B>
}