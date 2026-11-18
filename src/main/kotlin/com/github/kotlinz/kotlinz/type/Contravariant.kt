package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1

/**
 * A contravariant functor: a context that consumes values, mapped by pre-composing a function.
 * Typical examples are predicates, comparators and encoders.
 *
 * Laws:
 * - Identity: `contramap(id, v) == v`
 * - Composition: `contramap(f . g, v) == contramap(g, contramap(f, v))`
 *
 * @see Functor
 * @see com.github.kotlinz.kotlinz.type.divisible.Divisible
 */
interface Contravariant<T>: Invariant<T> {
  /** Turns a consumer of `A` into a consumer of `B` by converting inputs with `f` first. */
  fun <A, B> contramap(f: (B) -> A, v: K1<T, A>): K1<T, B>
  /** Implements [Invariant.imap] by ignoring the forward function. */
  override fun <A, B> imap(f: (A) -> B, g: (B) -> A, v: K1<T, A>): K1<T, B> = contramap(g, v)
}
