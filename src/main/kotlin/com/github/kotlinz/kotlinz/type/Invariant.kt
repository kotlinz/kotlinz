package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1

/**
 * An invariant functor: a type constructor that can be mapped given functions in both directions.
 *
 * Laws:
 * - Identity: `imap(id, id, v) == v`
 * - Composition: `imap(g1, g2, imap(f1, f2, v)) == imap(g1 . f1, f2 . g2, v)`
 *
 * @see Functor
 * @see Contravariant
 */
interface Invariant<T> {
  /** Maps `v` with `f`, using `g` to map values flowing in the opposite direction. */
  fun <A, B> imap(f: (A) -> B, g: (B) -> A, v: K1<T, A>): K1<T, B>
}
