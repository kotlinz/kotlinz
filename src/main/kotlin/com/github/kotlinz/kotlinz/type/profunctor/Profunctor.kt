package com.github.kotlinz.kotlinz.type.profunctor

import com.github.kotlinz.kotlinz.K3

/**
 * A two-argument type constructor that is contravariant in its input and covariant in its output.
 *
 * Uses the same `K3<T, F, A, B>` encoding as [com.github.kotlinz.kotlinz.type.category.Compose].
 *
 * Laws:
 * - Identity: `dimap(id, id, v) == v`
 * - Composition: `dimap(f1 . f2, g2 . g1, v) == dimap(f2, g2, dimap(f1, g1, v))`
 *
 * @see Strong
 * @see Choice
 */
interface Profunctor<T, F> {
  /** Pre-composes `f` on the input and post-composes `g` on the output. */
  fun <A, B, C, D> dimap(f: (A) -> B, g: (C) -> D, v: K3<T, F, B, C>): K3<T, F, A, D>
  /** Maps only the input. */
  fun <A, B, C> lmap(f: (A) -> B, v: K3<T, F, B, C>): K3<T, F, A, C> = dimap(f, { it }, v)
  /** Maps only the output. */
  fun <A, B, C> rmap(g: (B) -> C, v: K3<T, F, A, B>): K3<T, F, A, C> = dimap({ it }, g, v)
}
