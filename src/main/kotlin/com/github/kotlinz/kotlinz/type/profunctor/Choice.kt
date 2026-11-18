package com.github.kotlinz.kotlinz.type.profunctor

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either

/**
 * A profunctor that can pass through the other branch of an `Either` (the basis of prisms).
 *
 * Laws:
 * - `left(v) == dimap(Either::mirror, Either::mirror, right(v))`
 * - `rmap(::Left, v) == lmap(::Left, left(v))`
 * - `left(left(v)) == dimap(assocE, unassocE, left(v))`
 *
 * @see Profunctor
 * @see com.github.kotlinz.kotlinz.type.arrow.ArrowChoice
 */
interface Choice<T, F> : Profunctor<T, F> {
  /** Lifts `v` to act on `Left` values, passing `Right` values through. */
  fun <A, B, C> left(v: K3<T, F, A, B>): K3<T, F, Either<A, C>, Either<B, C>>

  /** Lifts `v` to act on `Right` values, passing `Left` values through. */
  fun <A, B, C> right(v: K3<T, F, A, B>): K3<T, F, Either<C, A>, Either<C, B>> =
    dimap({ e: Either<C, A> -> e.mirror() }, { e: Either<B, C> -> e.mirror() }, left<A, B, C>(v))
}

private fun <X, Y> Either<X, Y>.mirror(): Either<Y, X> = when (this) {
  is Either.Left -> Either.Right(value)
  is Either.Right -> Either.Left(value)
}
