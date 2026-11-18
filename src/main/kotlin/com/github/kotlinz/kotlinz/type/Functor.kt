package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1

/**
 * A covariant functor: a context whose contents can be transformed without changing its shape.
 *
 * Laws:
 * - Identity: `fmap(id, v) == v`
 * - Composition: `fmap(g . f, v) == fmap(g, fmap(f, v))`
 *
 * @see Invariant
 * @see com.github.kotlinz.kotlinz.type.applicative.Applicative
 */
interface Functor<T>: Invariant<T> {
  /** Applies `f` to every value inside `v`. */
  fun <A, B> fmap(f: (A) -> B, v: K1<T, A>): K1<T, B>
  /** Implements [Invariant.imap] by ignoring the backward function. */
  override fun <A, B> imap(f: (A) -> B, g: (B) -> A, v: K1<T, A>): K1<T, B> = fmap(f, v)
}