package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.type.pointed.Pointed

/**
 * An applicative functor: an [Apply] that can also lift plain values with [Pointed.pure], so it can
 * apply a function in a context to a value in a context, combining independent effects.
 *
 * Laws:
 * - Identity: `ap(pure(id), v) == v`
 * - Homomorphism: `ap(pure(f), pure(x)) == pure(f(x))`
 * - Interchange: `ap(u, pure(y)) == ap(pure { f -> f(y) }, u)`
 * - Composition: `ap(ap(ap(pure(compose), u), v), w) == ap(u, ap(v, w))`
 * - Consistency with Functor: `fmap(f, v) == ap(pure(f), v)`
 *
 * @see Apply
 * @see com.github.kotlinz.kotlinz.type.pointed.Pointed
 * @see com.github.kotlinz.kotlinz.type.monad.Monad
 */
interface Applicative<T>: Apply<T>, Pointed<T>
