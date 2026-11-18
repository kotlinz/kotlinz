package com.github.kotlinz.kotlinz.type.divisible

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Contravariant

/**
 * The contravariant analogue of an applicative: consumers that can be split across the parts of a pair.
 *
 * Laws:
 * - `divide(f, v, conquer()) == contramap({ f(it).first }, v)`
 * - `divide(f, conquer(), v) == contramap({ f(it).second }, v)`
 * - `divide` is associative up to re-association of the pairs
 *
 * @see com.github.kotlinz.kotlinz.type.Contravariant
 * @see Decidable
 */
interface Divisible<T>: Contravariant<T> {
  /** Splits an `A` with `f` and feeds the parts to `fb` and `fc`. */
  fun <A, B, C> divide(f: (A) -> Pair<B, C>, fb: K1<T, B>, fc: K1<T, C>): K1<T, A>
  /** The consumer that ignores its input; the identity of [divide]. */
  fun <A> conquer(): K1<T, A>
}
