package com.github.kotlinz.kotlinz.type.divisible

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either

/**
 * The contravariant analogue of an alternative: consumers that can branch on an `Either`.
 *
 * Laws:
 * - `choose(::Left, v, lose(f)) == v`
 * - `choose(::Right, lose(f), v) == v`
 * - `choose` is associative up to re-association of the `Either`s
 *
 * @see Divisible
 */
interface Decidable<T>: Divisible<T> {
  /** Classifies an `A` with `f` and feeds it to `fb` or `fc` accordingly. */
  fun <A, B, C> choose(f: (A) -> Either<B, C>, fb: K1<T, B>, fc: K1<T, C>): K1<T, A>
  /** The consumer for an uninhabited input; the identity of [choose]. */
  fun <A> lose(f: (A) -> Nothing): K1<T, A>
}
