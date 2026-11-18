package com.github.kotlinz.kotlinz.type.pointed

import com.github.kotlinz.kotlinz.K1

/**
 * A type constructor that can lift a plain value into a minimal context.
 *
 * On its own it has no laws; they come from the classes that extend it, such as [com.github.kotlinz.kotlinz.type.applicative.Applicative].
 *
 * @see Copointed
 */
interface Pointed<T> {
  /** Wraps `v` in the minimal context. */
  fun <A> pure(v: A): K1<T, A>
}
